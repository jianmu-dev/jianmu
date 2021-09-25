package dev.jianmu.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.embedded.worker.aggregate.DockerTask;
import dev.jianmu.embedded.worker.aggregate.DockerWorker;
import dev.jianmu.embedded.worker.aggregate.spec.ContainerSpec;
import dev.jianmu.embedded.worker.aggregate.spec.HostConfig;
import dev.jianmu.embedded.worker.aggregate.spec.Mount;
import dev.jianmu.embedded.worker.aggregate.spec.MountType;
import dev.jianmu.infrastructure.storage.StorageService;
import dev.jianmu.worker.aggregate.WorkerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @class: EmbeddedWorkerApplication
 * @description: EmbeddedWorkerApplication
 * @author: Ethan Liu
 * @create: 2021-09-12 22:23
 **/
@Service
@Slf4j
public class EmbeddedWorkerApplication {
    private final StorageService storageService;
    private final DockerWorker dockerWorker;
    private final ObjectMapper objectMapper;

    public EmbeddedWorkerApplication(
            StorageService storageService,
            DockerWorker dockerWorker,
            ObjectMapper objectMapper
    ) {
        this.storageService = storageService;
        this.dockerWorker = dockerWorker;
        this.objectMapper = objectMapper;
    }

    public void createVolume(String volumeName) {
        this.dockerWorker.createVolume(volumeName);
    }

    public void deleteVolume(String volumeName) {
        this.dockerWorker.deleteVolume(volumeName);
    }

    public void runTask(WorkerTask workerTask) {
        var parameterMap = workerTask.getParameterMap().entrySet().stream()
                .map(entry -> Map.entry("JIANMU_" + entry.getKey().toUpperCase(), entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        parameterMap.put("JIANMU_SHARE_DIR", "/" + workerTask.getTriggerId());
        parameterMap.put("JM_SHARE_DIR", "/" + workerTask.getTriggerId());
        try {
            var dockerTask = this.createDockerTask(workerTask, parameterMap);
            // 创建logWriter
            var logWriter = this.storageService.writeLog(workerTask.getTaskInstanceId());
            // 执行任务
            this.dockerWorker.runTask(dockerTask, logWriter);
        } catch (RuntimeException | JsonProcessingException e) {
            log.error("任务执行失败：", e);
            throw new RuntimeException("任务执行失败");
        }
    }

    private DockerTask createDockerTask(WorkerTask workerTask, Map<String, String> environmentMap) throws JsonProcessingException {
        var spec = objectMapper.readValue(workerTask.getSpec(), ContainerSpec.class);
        var env = environmentMap.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue()).toArray(String[]::new);
        // 使用TriggerId作为工作目录名称与volume名称
        var workingDir = "/" + workerTask.getTriggerId();
        var volumeName = workerTask.getTriggerId();

        var mount = Mount.Builder.aMount()
                .type(MountType.VOLUME)
                .source(volumeName)
                .target(workingDir)
                .build();
        var hostConfig = HostConfig.Builder.aHostConfig().mounts(List.of(mount)).build();
        var newSpec = ContainerSpec.builder()
                .image(spec.getImage())
                .workingDir("")
                .hostConfig(hostConfig)
                .cmd(spec.getCmd())
                .entrypoint(spec.getEntrypoint())
                .env(env)
                .build();
        return DockerTask.Builder.aDockerTask()
                .taskInstanceId(workerTask.getTaskInstanceId())
                .businessId(workerTask.getBusinessId())
                .triggerId(workerTask.getTriggerId())
                .defKey(workerTask.getDefKey())
                .resultFile(workerTask.getResultFile())
                .spec(newSpec)
                .build();
    }
}
