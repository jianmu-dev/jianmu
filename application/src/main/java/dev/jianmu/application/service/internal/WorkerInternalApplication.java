package dev.jianmu.application.service.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.query.NodeDefApi;
import dev.jianmu.infrastructure.docker.ContainerSpec;
import dev.jianmu.infrastructure.docker.TaskFailedEvent;
import dev.jianmu.infrastructure.docker.TaskFinishedEvent;
import dev.jianmu.infrastructure.docker.TaskRunningEvent;
import dev.jianmu.infrastructure.storage.StorageService;
import dev.jianmu.infrastructure.worker.DeferredResultService;
import dev.jianmu.infrastructure.worker.DispatchWorker;
import dev.jianmu.secret.aggregate.CredentialManager;
import dev.jianmu.secret.aggregate.KVPair;
import dev.jianmu.task.aggregate.InstanceParameter;
import dev.jianmu.task.aggregate.InstanceStatus;
import dev.jianmu.task.aggregate.NodeInfo;
import dev.jianmu.task.aggregate.TaskInstance;
import dev.jianmu.task.event.TaskInstanceCreatedEvent;
import dev.jianmu.task.repository.InstanceParameterRepository;
import dev.jianmu.task.repository.TaskInstanceRepository;
import dev.jianmu.trigger.repository.TriggerEventRepository;
import dev.jianmu.worker.aggregate.Worker;
import dev.jianmu.worker.repository.WorkerRepository;
import dev.jianmu.workflow.aggregate.parameter.Parameter;
import dev.jianmu.workflow.aggregate.parameter.SecretParameter;
import dev.jianmu.workflow.aggregate.process.WorkflowInstance;
import dev.jianmu.workflow.repository.ParameterRepository;
import dev.jianmu.workflow.repository.WorkflowInstanceRepository;
import dev.jianmu.workflow.service.ParameterDomainService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @class WorkerInternalApplication
 * @description WorkerInternalApplication
 * @author Daihw
 * @create 2022/5/27 10:46 上午
 */
@Slf4j
@Service
public class WorkerInternalApplication {
    private static final Logger logger = LoggerFactory.getLogger(WorkerInternalApplication.class);
    private final String optionScript = "set -e";
    private final String traceScript = "\necho + %s\n%s";

    private final ParameterRepository parameterRepository;
    private final ParameterDomainService parameterDomainService;
    private final CredentialManager credentialManager;
    private final NodeDefApi nodeDefApi;
    private final WorkerRepository workerRepository;
    private final ApplicationEventPublisher publisher;
    private final InstanceParameterRepository instanceParameterRepository;
    private final TriggerEventRepository triggerEventRepository;
    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final DeferredResultService deferredResultService;
    private final TaskInstanceRepository taskInstanceRepository;
    private final ObjectMapper objectMapper;
    private final StorageService storageService;

    public WorkerInternalApplication(
            ParameterRepository parameterRepository,
            ParameterDomainService parameterDomainService,
            CredentialManager credentialManager,
            NodeDefApi nodeDefApi,
            WorkerRepository workerRepository,
            ApplicationEventPublisher publisher,
            InstanceParameterRepository instanceParameterRepository,
            TriggerEventRepository triggerEventRepository,
            WorkflowInstanceRepository workflowInstanceRepository,
            DeferredResultService deferredResultService, TaskInstanceRepository taskInstanceRepository, ObjectMapper objectMapper, StorageService storageService) {
        this.parameterRepository = parameterRepository;
        this.parameterDomainService = parameterDomainService;
        this.credentialManager = credentialManager;
        this.nodeDefApi = nodeDefApi;
        this.workerRepository = workerRepository;
        this.publisher = publisher;
        this.instanceParameterRepository = instanceParameterRepository;
        this.triggerEventRepository = triggerEventRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.deferredResultService = deferredResultService;
        this.taskInstanceRepository = taskInstanceRepository;
        this.objectMapper = objectMapper;
        this.storageService = storageService;
    }

    @Transactional
    public void join(String workerId, Worker.Type type, String name) {
        if (this.workerRepository.findById(workerId).isPresent()) {
            return;
        }
        this.workerRepository.add(Worker.Builder.aWorker()
                .id(workerId)
                .name(name)
                .type(type)
                .status(Worker.Status.ONLINE)
                .build());
    }

    @Transactional
    public void dispatchTask(TaskInstanceCreatedEvent event) {
        var taskInstance = this.taskInstanceRepository.findById(event.getTaskInstanceId())
                .orElseThrow(() -> new RuntimeException("未找到任务实例：" + event.getTaskInstanceId()));
        try {
            if (!taskInstance.isVolume()) {
                var nodeDef = this.nodeDefApi.findByType(taskInstance.getDefKey());
                if (!nodeDef.getWorkerType().equals("DOCKER")) {
                    throw new RuntimeException("无法执行此类节点任务: " + nodeDef.getType());
                }
            }
            this.workflowInstanceRepository.findByTriggerId(event.getTriggerId())
                    .ifPresent(workflowInstance -> {
                        // 分发worker
                        var workers = this.workerRepository.findByTypeAndCreatedTimeLessThan(Worker.Type.DOCKER, workflowInstance.getStartTime());
                        if (workers.isEmpty()) {
                            throw new RuntimeException("worker数量为0，类型：" + Worker.Type.DOCKER);
                        }
                        var worker = DispatchWorker.getWorker(taskInstance.getTriggerId(), workers);
                        taskInstance.setWorkerId(worker.getId());
                        this.taskInstanceRepository.updateWorkerId(taskInstance);
                        // 返回DeferredResult
                        this.deferredResultService.clearWorker(worker.getId());
                    });
        } catch (RuntimeException e) {
            logger.error("任务分发失败，", e);
            taskInstance.dispatchFailed();
            this.taskInstanceRepository.updateStatus(taskInstance);
        }
    }

    @Transactional
    public void createVolumeTask(String triggerId, String defKey) {
        this.workflowInstanceRepository.findByTriggerId(triggerId)
                .ifPresent(workflow -> this.taskInstanceRepository.add(TaskInstance.Builder.anInstance()
                        .serialNo(1)
                        .defKey(defKey)
                        .nodeInfo(NodeInfo.Builder.aNodeDef().name(defKey).build())
                        .asyncTaskRef(defKey)
                        .workflowRef(workflow.getWorkflowRef())
                        .workflowVersion(workflow.getWorkflowVersion())
                        .businessId(UUID.randomUUID().toString().replace("-", ""))
                        .triggerId(triggerId)
                        .build()));
    }

    public Optional<TaskInstance> pullTasks(String workerId) {
        return this.taskInstanceRepository.findByWorkerIdAndMinVersion(workerId);
    }

    public ContainerSpec getContainerSpec(TaskInstance taskInstance) {
        // 查找节点定义
        var nodeDef = this.nodeDefApi.findByType(taskInstance.getDefKey());
        if (!nodeDef.getWorkerType().equals("DOCKER")) {
            throw new RuntimeException("无法执行此类节点任务: " + nodeDef.getType());
        }
        // 环境变量
        var worker = this.workerRepository.findById(taskInstance.getWorkerId())
                .orElseThrow(() -> new RuntimeException("未找到worker：" + taskInstance.getWorkerId()));
        var instanceParameters = this.instanceParameterRepository
                .findByInstanceIdAndType(taskInstance.getId(), InstanceParameter.Type.INPUT);
        var parameterMap = this.getParameterMap(instanceParameters);
        parameterMap.putAll(this.getEnvVariable(taskInstance, worker));
        this.addFeatureParam(parameterMap);
        parameterMap = parameterMap.entrySet().stream()
                .filter(entry -> entry.getKey() != null)
                .map(entry -> Map.entry(entry.getKey().toUpperCase(), entry.getValue() == null ? "" : entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // 创建ContainerSpec
        ContainerSpec newSpec;
        if (nodeDef.getImage() != null) {
            var script = this.createScript(nodeDef.getScript());
            parameterMap.put("JIANMU_SCRIPT", script);
            String[] entrypoint = {"/bin/sh", "-c"};
            String[] args = {"echo \"$JIANMU_SCRIPT\" | /bin/sh"};
            newSpec = ContainerSpec.builder()
                    .image(nodeDef.getImage())
                    .working_dir("/" + taskInstance.getTriggerId())
                    .environment(parameterMap)
                    .entrypoint(entrypoint)
                    .args(args)
                    .build();
        } else {
            parameterMap = parameterMap.entrySet().stream()
                    .filter(entry -> entry.getKey() != null)
                    .map(entry -> Map.entry("JIANMU_" + entry.getKey(), entry.getValue() == null ? "" : entry.getValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            dev.jianmu.embedded.worker.aggregate.spec.ContainerSpec spec;
            try {
                spec = objectMapper.readValue(nodeDef.getSpec(), dev.jianmu.embedded.worker.aggregate.spec.ContainerSpec.class);
            } catch (JsonProcessingException e) {
                log.error("拉取任务失败：", e);
                throw new RuntimeException("拉取任务失败");
            }
            newSpec = ContainerSpec.builder()
                    .image(spec.getImage())
                    .working_dir("/" + taskInstance.getTriggerId())
                    .user(spec.getUser())
                    .host(spec.getHostName())
                    .environment(parameterMap)
                    .entrypoint(spec.getEntrypoint())
                    .args(spec.getCmd())
                    .volume_mounts(spec.getHostConfig() == null || spec.getHostConfig().getMounts() == null ? null : (String[]) spec.getHostConfig().getMounts().toArray())
                    .build();
        }
        return newSpec;
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

    private Map<String, String> getParameterMap(List<InstanceParameter> instanceParameters) {
        var parameterMap = instanceParameters.stream()
                .map(instanceParameter -> Map.entry(
                        instanceParameter.getRef(),
                        instanceParameter.getParameterId()
                        )
                )
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        // 查询参数值
        var parameters = this.parameterRepository.findByIds(new HashSet<>(parameterMap.values()));
        var secretParameters = parameters.stream()
                .filter(parameter -> parameter instanceof SecretParameter)
                // 过滤非正常语法
                .filter(parameter -> parameter.getStringValue().split("\\.").length == 2)
                .collect(Collectors.toList());
        // 替换密钥参数值
        this.handleSecretParameter(parameterMap, secretParameters);
        // 替换实际参数值
        this.parameterDomainService.createParameterMap(parameterMap, parameters);
        return parameterMap;
    }

    private void handleSecretParameter(Map<String, String> parameterMap, List<Parameter> secretParameters) {
        parameterMap.forEach((key, val) -> {
            secretParameters.stream()
                    .filter(parameter -> parameter.getId().equals(val))
                    .findFirst()
                    .ifPresent(parameter -> {
                        var kvPairOptional = this.findSecret(parameter);
                        kvPairOptional.ifPresent(kv -> {
                            var secretParameter = Parameter.Type.STRING.newParameter(kv.getValue());
                            parameterMap.put(key, secretParameter.getStringValue());
                        });
                    });
        });
    }

    private Optional<KVPair> findSecret(Parameter<?> parameter) {
        Parameter<?> secretParameter;
        // 处理密钥类型参数, 获取值后转换为String类型参数
        var strings = parameter.getStringValue().split("\\.");
        return this.credentialManager.findByNamespaceNameAndKey(strings[0], strings[1]);
    }

    /**
     * 设置一些通用参数到环境变量,方便在DSL中使用
     *
     * @param taskInstance
     * @param worker
     * @return
     */
    private HashMap<String, String> getEnvVariable(TaskInstance taskInstance, Worker worker) {

        HashMap<String, String> env = new HashMap<>();
        env.put("JIANMU_SHARE_DIR", "/" + taskInstance.getTriggerId());
        env.put("JM_SHARE_DIR", "/" + taskInstance.getTriggerId());

        env.put("JM_WORKER_ID", worker.getId());
        env.put("JM_WORKER_TYPE", worker.getType().name());
        env.put("JM_BUSINESS_ID", taskInstance.getBusinessId());
        env.put("JM_TRIGGER_ID", taskInstance.getTriggerId());
        env.put("JM_DEF_KEY", taskInstance.getDefKey());

        var triggerEvent = this.triggerEventRepository.findById(taskInstance.getTriggerId())
                .orElseThrow(() -> new DataNotFoundException("未找到该触发事件"));
        env.put("JM_PROJECT_ID", triggerEvent.getProjectId());
        env.put("JM_WEB_REQUEST_ID", triggerEvent.getWebRequestId());
        env.put("JM_TRIGGER_TIME", formatTime(triggerEvent.getOccurredTime()));
        env.put("JM_TRIGGER_TYPE", triggerEvent.getTriggerType());

        // workflow instance 相关参数
        WorkflowInstance workflowInstance = workflowInstanceRepository
                .findByTriggerId(taskInstance.getTriggerId())
                .orElseThrow(() -> new DataNotFoundException("未找到该workflow instance"));

        env.put("JM_INSTANCE_ID", workflowInstance.getId());
        env.put("JM_INSTANCE_TRIGGER_TYPE", workflowInstance.getTriggerType());
        env.put("JM_INSTANCE_WORKFLOW_REF", workflowInstance.getWorkflowRef());
        env.put("JM_INSTANCE_WORKFLOW_VERSION", workflowInstance.getWorkflowVersion());
        env.put("JM_INSTANCE_CREATE_TIME", formatTime(workflowInstance.getCreateTime()));
        env.put("JM_INSTANCE_START_TIME", formatTime(workflowInstance.getStartTime()));
        env.put("JM_INSTANCE_SUSPENDED_TIME", formatTime(workflowInstance.getSuspendedTime()));
        env.put("JM_INSTANCE_SERIAL_NO", workflowInstance.getSerialNo() + "");
        env.put("JM_INSTANCE_RUN_MODE", workflowInstance.getRunMode().name());
        env.put("JM_INSTANCE_STATUS", workflowInstance.getStatus().name());

        return env;
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? "" : time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 添加新的环境变量,以为JIANMU_开头的变量,都复制一份以为JM_开头的变量
     *
     * @param parameterMap
     * @return
     */
    private Map<String, String> addFeatureParam(Map<String, String> parameterMap) {
        var oldParameterMap = new HashMap<>(parameterMap);

        parameterMap.clear();
        oldParameterMap.forEach((key, value) -> {
            if (key != null && key.startsWith("JIANMU_")) {
                parameterMap.put(key.replaceFirst("JIANMU_", "JM_"), value);
            }
            parameterMap.put(key, value);
        });

        return parameterMap;
    }

    @Transactional
    public TaskInstance acceptTask(HttpServletResponse response, String workerId, String taskInstanceId, int version) {
        var taskInstance = this.taskInstanceRepository.findByIdAndVersion(taskInstanceId, version)
                .orElse(null);
        if (taskInstance == null) {
            response.setStatus(HttpStatus.SC_CONFLICT);
            return null;
        }
        if (!taskInstance.isDeletionVolume()) {
            var workflowInstance = this.workflowInstanceRepository.findByTriggerId(taskInstance.getTriggerId())
                    .orElseThrow(() -> new RuntimeException("未找到流程实例"));
            if (!workflowInstance.isRunning()) {
                response.setStatus(HttpStatus.SC_GONE);
                return null;
            }
        }
        taskInstance.acceptTask(version);
        if (!this.taskInstanceRepository.acceptTask(taskInstance)) {
            response.setStatus(HttpStatus.SC_CONFLICT);
        }
        return taskInstance;
    }

    @Transactional
    public void updateTaskInstance(String workerId, String taskInstanceId, String status, String resultFile, String errorMsg, Integer exitCode) {
        switch (status) {
            case "RUNNING":
                this.publisher.publishEvent(TaskRunningEvent.builder()
                        .taskId(taskInstanceId)
                        .build());
                break;
            case "FAILED":
                this.publisher.publishEvent(TaskFailedEvent.builder()
                        .taskId(taskInstanceId)
                        .errorMsg(errorMsg)
                        .build());
                break;
            case "SUCCEED":
                this.publisher.publishEvent(TaskFinishedEvent.builder()
                        .taskId(taskInstanceId)
                        .cmdStatusCode(exitCode)
                        .resultFile(resultFile)
                        .build());
                break;
        }
    }

    public void writeTaskLog(String workerId, String taskInstanceId, String content, Long number, Long timestamp) {
        if (content == null) {
            return;
        }
        try (var logWriter = this.storageService.writeLog(taskInstanceId)) {
            logWriter.write(content);
            logWriter.flush();
        } catch (IOException e) {
            logger.error("任务日志写入失败：", e);
        }
    }

    public void terminateTask(String workerId, String taskInstanceId) {
        this.deferredResultService.terminateDeferredResult(workerId, taskInstanceId);
    }
}
