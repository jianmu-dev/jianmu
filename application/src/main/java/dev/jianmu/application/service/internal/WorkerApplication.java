package dev.jianmu.application.service.internal;

import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.query.NodeDefApi;
import dev.jianmu.secret.aggregate.CredentialManager;
import dev.jianmu.secret.aggregate.KVPair;
import dev.jianmu.task.aggregate.InstanceParameter;
import dev.jianmu.task.event.TaskInstanceCreatedEvent;
import dev.jianmu.task.repository.InstanceParameterRepository;
import dev.jianmu.trigger.repository.TriggerEventRepository;
import dev.jianmu.worker.aggregate.Worker;
import dev.jianmu.worker.aggregate.WorkerTask;
import dev.jianmu.worker.event.CleanupWorkspaceEvent;
import dev.jianmu.worker.event.CreateWorkspaceEvent;
import dev.jianmu.worker.event.TerminateTaskEvent;
import dev.jianmu.worker.repository.WorkerRepository;
import dev.jianmu.workflow.aggregate.parameter.Parameter;
import dev.jianmu.workflow.aggregate.parameter.SecretParameter;
import dev.jianmu.workflow.aggregate.process.WorkflowInstance;
import dev.jianmu.workflow.repository.ParameterRepository;
import dev.jianmu.workflow.repository.WorkflowInstanceRepository;
import dev.jianmu.workflow.service.ParameterDomainService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Ethan Liu
 * @class WorkerApplication
 * @description 任务执行器门面类
 * @create 2021-04-02 12:30
 */
@Service
@Transactional
public class WorkerApplication {
    private final ParameterRepository parameterRepository;
    private final ParameterDomainService parameterDomainService;
    private final CredentialManager credentialManager;
    private final NodeDefApi nodeDefApi;
    private final WorkerRepository workerRepository;
    private final ApplicationEventPublisher publisher;
    private final InstanceParameterRepository instanceParameterRepository;
    private final TriggerEventRepository triggerEventRepository;
    private final WorkflowInstanceRepository workflowInstanceRepository;
    public WorkerApplication(
            ParameterRepository parameterRepository,
            ParameterDomainService parameterDomainService,
            CredentialManager credentialManager,
            NodeDefApi nodeDefApi,
            WorkerRepository workerRepository,
            ApplicationEventPublisher publisher,
            InstanceParameterRepository instanceParameterRepository,
            TriggerEventRepository triggerEventRepository,
            WorkflowInstanceRepository workflowInstanceRepository
    ) {
        this.parameterRepository = parameterRepository;
        this.parameterDomainService = parameterDomainService;
        this.credentialManager = credentialManager;
        this.nodeDefApi = nodeDefApi;
        this.workerRepository = workerRepository;
        this.publisher = publisher;
        this.instanceParameterRepository = instanceParameterRepository;
        this.triggerEventRepository = triggerEventRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
    }

    private Worker findWorker() {
        // 查找符合条件的Worker
        // TODO 暂时全部分配给内置Worker
        var worker = this.workerRepository.findByType(Worker.Type.EMBEDDED);
        return worker;
    }

    public void createWorkspace(String triggerId) {
        var worker = this.findWorker();
        this.publisher.publishEvent(
                CreateWorkspaceEvent.Builder.aCreateWorkspaceEvent()
                        .workerId(worker.getId())
                        .workerType(worker.getType().name())
                        .workspaceName(triggerId)
                        .build()
        );
    }

    public void cleanupWorkspace(String triggerId) {
        var worker = this.findWorker();
        this.publisher.publishEvent(
                CleanupWorkspaceEvent.Builder.aCleanupWorkspaceEvent()
                        .workerId(worker.getId())
                        .workerType(worker.getType().name())
                        .workspaceName(triggerId)
                        .build()
        );
    }

    public void terminateTask(String taskInstanceId) {
        var worker = this.findWorker();
        this.publisher.publishEvent(
                TerminateTaskEvent.Builder.aTerminateTaskEvent()
                        .workerId(worker.getId())
                        .workerType(worker.getType().name())
                        .taskInstanceId(taskInstanceId)
                        .build()
        );
    }

    public void dispatchTask(TaskInstanceCreatedEvent event, boolean resumed) {
        // 查找节点定义
        var nodeDef = this.nodeDefApi.findByType(event.getDefKey());
        if (!nodeDef.getWorkerType().equals("DOCKER")) {
            throw new RuntimeException("无法执行此类节点任务: " + nodeDef.getType());
        }
        var worker = this.findWorker();
        // 创建WorkerTask
        var instanceParameters = this.instanceParameterRepository
                .findByInstanceIdAndType(event.getTaskInstanceId(), InstanceParameter.Type.INPUT);
        var parameterMap = this.getParameterMap(instanceParameters);
        parameterMap.putAll(this.getEnvVariable(event, worker));
        this.addFeatureParam(parameterMap);
        WorkerTask workerTask;
        if (nodeDef.getImage() != null) {
            workerTask = WorkerTask.Builder.aWorkerTask()
                    .workerId(worker.getId())
                    .type(worker.getType())
                    .taskInstanceId(event.getTaskInstanceId())
                    .businessId(event.getBusinessId())
                    .triggerId(event.getTriggerId())
                    .defKey(event.getDefKey())
                    .parameterMap(parameterMap)
                    .resumed(resumed)
                    .shellTask(true)
                    .image(nodeDef.getImage())
                    .script(nodeDef.getScript())
                    .build();
            // 发送给Worker执行
        } else {
            workerTask = WorkerTask.Builder.aWorkerTask()
                    .workerId(worker.getId())
                    .type(worker.getType())
                    .taskInstanceId(event.getTaskInstanceId())
                    .businessId(event.getBusinessId())
                    .triggerId(event.getTriggerId())
                    .defKey(event.getDefKey())
                    .resultFile(nodeDef.getResultFile())
                    .spec(nodeDef.getSpec())
                    .parameterMap(parameterMap)
                    .resumed(resumed)
                    .shellTask(false)
                    .build();
            // 发送给Worker执行
        }
        this.publisher.publishEvent(workerTask);
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
     * @param workerTask 工作任务
     * @return 环境变量
     */
    private HashMap<String, String> getEnvVariable(TaskInstanceCreatedEvent event,Worker worker) {

        HashMap<String, String> env = new HashMap<>();
        env.put("JIANMU_SHARE_DIR", "/" + event.getTriggerId());
        env.put("JM_SHARE_DIR", "/" + event.getTriggerId());

        env.put("JM_WORKER_ID", worker.getId());
        env.put("JM_WORKER_TYPE", worker.getType().name());
        env.put("JM_BUSINESS_ID", event.getBusinessId());
        env.put("JM_TRIGGER_ID", event.getTriggerId());
        env.put("JM_DEF_KEY", event.getDefKey());

        var triggerEvent = this.triggerEventRepository.findById(event.getTriggerId())
                                               .orElseThrow(() -> new DataNotFoundException("未找到该触发事件"));
        env.put("JM_PROJECT_ID", triggerEvent.getProjectId());
        env.put("JM_WEB_REQUEST_ID", triggerEvent.getWebRequestId());
        env.put("JM_TRIGGER_TIME", formatTime(triggerEvent.getOccurredTime()));
        env.put("JM_TRIGGER_TYPE", triggerEvent.getTriggerType());

        // workflow instance 相关参数
        WorkflowInstance workflowInstance = workflowInstanceRepository
                .findByTriggerId(event.getTriggerId())
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
}
