package dev.jianmu.application.service.internal;

import com.google.common.hash.Hashing;
import dev.jianmu.application.query.NodeDefApi;
import dev.jianmu.secret.aggregate.CredentialManager;
import dev.jianmu.secret.aggregate.KVPair;
import dev.jianmu.task.aggregate.InstanceParameter;
import dev.jianmu.task.event.TaskInstanceCreatedEvent;
import dev.jianmu.task.repository.InstanceParameterRepository;
import dev.jianmu.worker.aggregate.Worker;
import dev.jianmu.worker.aggregate.WorkerTask;
import dev.jianmu.worker.event.CleanupWorkspaceEvent;
import dev.jianmu.worker.event.CreateWorkspaceEvent;
import dev.jianmu.worker.event.TerminateTaskEvent;
import dev.jianmu.worker.repository.WorkerRepository;
import dev.jianmu.workflow.aggregate.parameter.Parameter;
import dev.jianmu.workflow.aggregate.parameter.SecretParameter;
import dev.jianmu.workflow.event.process.ProcessStartedEvent;
import dev.jianmu.workflow.repository.ParameterRepository;
import dev.jianmu.workflow.service.ParameterDomainService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
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

    public WorkerApplication(
            ParameterRepository parameterRepository,
            ParameterDomainService parameterDomainService,
            CredentialManager credentialManager,
            NodeDefApi nodeDefApi,
            WorkerRepository workerRepository,
            ApplicationEventPublisher publisher,
            InstanceParameterRepository instanceParameterRepository
    ) {
        this.parameterRepository = parameterRepository;
        this.parameterDomainService = parameterDomainService;
        this.credentialManager = credentialManager;
        this.nodeDefApi = nodeDefApi;
        this.workerRepository = workerRepository;
        this.publisher = publisher;
        this.instanceParameterRepository = instanceParameterRepository;
    }

    private Worker findWorker() {
        // 查找符合条件的Worker
        // TODO 暂时全部分配给内置Worker
        var worker = this.workerRepository.findByType(Worker.Type.EMBEDDED);
        return worker;
    }

    public void createWorkspace(ProcessStartedEvent event) {
        var worker = this.findWorker();
        this.publisher.publishEvent(
                CreateWorkspaceEvent.Builder.aCreateWorkspaceEvent()
                        .workerId(worker.getId())
                        .workerType(worker.getType().name())
                        .workspaceName(event.getTriggerId())
                        .workflowVersion(event.getWorkflowVersion())
                        .workflowRef(event.getWorkflowRef())
                        .workflowInstanceId(event.getWorkflowInstanceId())
                        .triggerId(event.getTriggerId())
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

    public void terminateTask(String triggerId, String taskInstanceId) {
        var worker = this.findWorker();
        this.publisher.publishEvent(
                TerminateTaskEvent.Builder.aTerminateTaskEvent()
                        .workerId(worker.getId())
                        .workerType(worker.getType().name())
                        .taskInstanceId(taskInstanceId)
                        .triggerId(triggerId)
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
        WorkerTask workerTask;
        var taskName = Hashing.murmur3_128().hashString(event.getAsyncTaskRef(), StandardCharsets.UTF_8).toString();
        if (nodeDef.getImage() != null) {
            workerTask = WorkerTask.Builder.aWorkerTask()
                    .workerId(worker.getId())
                    .type(worker.getType())
                    .taskInstanceId(event.getTaskInstanceId())
                    .taskName(taskName)
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
                    .taskName(taskName)
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
}
