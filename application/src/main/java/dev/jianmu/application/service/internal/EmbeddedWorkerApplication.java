package dev.jianmu.application.service.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.query.NodeDef;
import dev.jianmu.application.query.NodeDefApi;
import dev.jianmu.embedded.worker.aggregate.EmbeddedWorker;
import dev.jianmu.embedded.worker.aggregate.EmbeddedWorkerTask;
import dev.jianmu.embedded.worker.aggregate.spec.ContainerSpec;
import dev.jianmu.embedded.worker.aggregate.spec.HostConfig;
import dev.jianmu.embedded.worker.aggregate.spec.Mount;
import dev.jianmu.embedded.worker.aggregate.spec.MountType;
import dev.jianmu.infrastructure.GlobalProperties;
import dev.jianmu.infrastructure.storage.StorageService;
import dev.jianmu.node.definition.event.NodeDeletedEvent;
import dev.jianmu.node.definition.event.NodeUpdatedEvent;
import dev.jianmu.worker.aggregate.WorkerTask;
import dev.jianmu.worker.event.CreateWorkspaceEvent;
import dev.jianmu.workflow.repository.WorkflowRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Ethan Liu
 * @class EmbeddedWorkerApplication
 * @description EmbeddedWorkerApplication
 * @create 2021-09-12 22:23
 */
@Service
@Slf4j
public class EmbeddedWorkerApplication {
    private final StorageService storageService;
    private final EmbeddedWorker embeddedWorker;
    private final ObjectMapper objectMapper;
    private final GlobalProperties properties;

    private final WorkflowRepository workflowRepository;
    private final NodeDefApi nodeDefApi;

    private final String optionScript = "set -e";
    private final String traceScript = "\necho + %s\n%s";

    public EmbeddedWorkerApplication(
            StorageService storageService,
            EmbeddedWorker embeddedWorker,
            ObjectMapper objectMapper,
            GlobalProperties properties,
            WorkflowRepository workflowRepository,
            NodeDefApi nodeDefApi
    ) {
        this.storageService = storageService;
        this.embeddedWorker = embeddedWorker;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.workflowRepository = workflowRepository;
        this.nodeDefApi = nodeDefApi;
    }

    private ContainerSpec toSpec(String triggerId, String taskName, NodeDef nodeDef) {
        if (nodeDef.getImage() != null) {
            String[] entrypoint = {"/bin/sh", "-c"};
            String[] cmd = {"echo \"$JIANMU_SCRIPT\" | /bin/sh"};
            return ContainerSpec.builder()
                    .image(nodeDef.getImage())
                    .workingDir("")
                    .cmd(cmd)
                    .entrypoint(entrypoint)
                    .build();
        }
        try {
            return objectMapper.readValue(nodeDef.getSpec(), ContainerSpec.class);
        } catch (JsonProcessingException e) {
            log.error("e:", e);
            throw new IllegalArgumentException("spec无法解析");
        }
    }

    public void createVolume(CreateWorkspaceEvent event) {
        log.info("start create volume: {}", event.getWorkspaceName());
        var workflow = this.workflowRepository.findByRefAndVersion(event.getWorkflowRef(), event.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义"));
        Map<String, ContainerSpec> specMap = new HashMap<>();
        workflow.findTasks().forEach(node -> {
            var nodeDef = this.nodeDefApi.findByType(node.getType());
            var taskName = Hashing.murmur3_128().hashString(node.getRef(), StandardCharsets.UTF_8).toString();
            specMap.put(taskName, toSpec(event.getTriggerId(), node.getRef(), nodeDef));
        });
        this.embeddedWorker.createVolume(event.getWorkspaceName(), specMap);
        log.info("create volume: {} completed", event.getWorkspaceName());
    }

    public void deleteVolume(String volumeName) {
        log.info("start delete volume: {}", volumeName);
        this.embeddedWorker.deleteVolume(volumeName);
        log.info("delete volume: {} completed", volumeName);
    }

    public void runTask(WorkerTask workerTask) {
        try {
            Map<String, String> parameterMap;
            if (workerTask.isShellTask()) {
                parameterMap = workerTask.getParameterMap().entrySet().stream()
                        .map(entry -> Map.entry(entry.getKey().toUpperCase(), entry.getValue()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            } else {
                parameterMap = workerTask.getParameterMap().entrySet().stream()
                        .map(entry -> Map.entry("JIANMU_" + entry.getKey().toUpperCase(), entry.getValue()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            }
            parameterMap.put("JIANMU_SHARE_DIR", "/" + workerTask.getTriggerId());
            parameterMap.put("JM_SHARE_DIR", "/" + workerTask.getTriggerId());
            parameterMap.put("JM_RESULT_FILE", "/" + workerTask.getTriggerId() + "/" + workerTask.getTaskName());
            var dockerTask = this.createDockerTask(workerTask, parameterMap);
            // 创建logWriter
            var logWriter = this.storageService.writeLog(workerTask.getTaskInstanceId());
            if (workerTask.isResumed()) {
                // 恢复任务执行
                this.embeddedWorker.resumeTask(dockerTask, logWriter);
            } else {
                // 执行任务
                this.embeddedWorker.runTask(dockerTask, logWriter);
            }
        } catch (RuntimeException | JsonProcessingException e) {
            log.error("任务执行失败：", e);
            throw new RuntimeException("任务执行失败");
        }
    }

    public void terminateTask(String triggerId, String taskInstanceId) {
        try {
            this.embeddedWorker.terminateTask(triggerId, taskInstanceId);
        } catch (RuntimeException e) {
            log.warn("无法终止任务, 任务终止失败");
        }
    }

    public void deleteImage(NodeDeletedEvent event) {
        try {
            var spec = objectMapper.readValue(event.getSpec(), ContainerSpec.class);
            log.info("删除镜像: {}", spec.getImage(this.properties.getWorker().getDocker().getRegistryUrl()));
            this.embeddedWorker.deleteImage(spec.getImage(this.properties.getWorker().getDocker().getRegistryUrl()));
            log.info("删除镜像: {}", spec.getImage(this.properties.getMirror()));
            this.dockerWorker.deleteImage(spec.getImage(this.properties.getMirror()));
        } catch (Exception e) {
            log.error("节点镜像删除失败：", e);
        }
    }

    public void updateImage(NodeUpdatedEvent event) {
        try {
            var spec = objectMapper.readValue(event.getSpec(), ContainerSpec.class);
            log.info("更新镜像: {}", spec.getImage(this.properties.getWorker().getDocker().getRegistryUrl()));
            this.embeddedWorker.updateImage(spec.getImage(this.properties.getWorker().getDocker().getRegistryUrl()));
            log.info("更新镜像: {}", spec.getImage(this.properties.getMirror()));
            this.dockerWorker.updateImage(spec.getImage(this.properties.getMirror()));
        } catch (JsonProcessingException e) {
            log.error("节点镜像更新失败：", e);
        }
    }

    private String createScript(List<String> commands) {
        var sb = new StringBuilder();
        sb.append(optionScript);
        var formatter = new Formatter(sb, Locale.ROOT);
        commands.forEach(cmd -> {
            var escaped = String.format("%s", cmd);
            escaped = escaped.replace("$", "\\$");
            formatter.format(traceScript, escaped, cmd);
        });
        return sb.toString();
    }

    private EmbeddedWorkerTask createDockerTask(WorkerTask workerTask, Map<String, String> environmentMap) throws JsonProcessingException {
        // 使用TriggerId作为工作目录名称与volume名称
        var workingDir = "/" + workerTask.getTriggerId();
        var volumeName = workerTask.getTriggerId();

        var mount = Mount.Builder.aMount()
                .type(MountType.VOLUME)
                .source(volumeName)
                .target(workingDir)
                .build();
        var hostConfig = HostConfig.Builder.aHostConfig().mounts(List.of(mount)).build();
        ContainerSpec newSpec;
        if (workerTask.isShellTask()) {
            var script = this.createScript(workerTask.getScript());
            environmentMap.put("JIANMU_SCRIPT", script);
            var env = environmentMap.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue()).toArray(String[]::new);
            String[] entrypoint = {"/bin/sh", "-c"};
            String[] cmd = {"echo \"$JIANMU_SCRIPT\" | /bin/sh"};
            newSpec = ContainerSpec.builder()
                    .image(workerTask.getImage())
                    .workingDir("")
                    .hostConfig(hostConfig)
                    .cmd(cmd)
                    .entrypoint(entrypoint)
                    .env(env)
                    .build();
        } else {
            var spec = objectMapper.readValue(workerTask.getSpec(), ContainerSpec.class);
            var env = environmentMap.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue()).toArray(String[]::new);
            newSpec = ContainerSpec.builder()
                    .image(spec.getImage())
                    .workingDir("")
                    .hostConfig(hostConfig)
                    .cmd(spec.getCmd())
                    .entrypoint(spec.getEntrypoint())
                    .env(env)
                    .build();
        }
        return EmbeddedWorkerTask.Builder.aEmbeddedWorkerTask()
                .taskInstanceId(workerTask.getTaskInstanceId())
                .taskName(workerTask.getTaskName())
                .businessId(workerTask.getBusinessId())
                .triggerId(workerTask.getTriggerId())
                .defKey(workerTask.getDefKey())
                .resultFile(workerTask.getResultFile())
                .spec(newSpec)
                .build();
    }
}
