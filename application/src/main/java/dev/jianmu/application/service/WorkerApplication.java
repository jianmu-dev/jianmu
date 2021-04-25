package dev.jianmu.application.service;

import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.dsl.repository.OutputParameterReferRepository;
import dev.jianmu.infrastructure.docker.DockerWorker;
import dev.jianmu.infrastructure.storage.StorageService;
import dev.jianmu.parameter.aggregate.Parameter;
import dev.jianmu.parameter.aggregate.SecretParameter;
import dev.jianmu.parameter.repository.ParameterRepository;
import dev.jianmu.parameter.repository.ReferenceRepository;
import dev.jianmu.parameter.service.ParameterDomainService;
import dev.jianmu.parameter.service.ReferenceDomainService;
import dev.jianmu.secret.repository.KVPairRepository;
import dev.jianmu.task.aggregate.Definition;
import dev.jianmu.task.aggregate.DockerDefinition;
import dev.jianmu.task.aggregate.TaskInstance;
import dev.jianmu.task.aggregate.Worker;
import dev.jianmu.task.repository.DefinitionRepository;
import dev.jianmu.task.repository.TaskInstanceRepository;
import dev.jianmu.task.repository.WorkerRepository;
import dev.jianmu.task.service.WorkerDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @class: WorkerApplication
 * @description: 任务执行器门面类
 * @author: Ethan Liu
 * @create: 2021-04-02 12:30
 **/
@Service
@Transactional
public class WorkerApplication {
    private static final Logger logger = LoggerFactory.getLogger(WorkerApplication.class);
    private final WorkerRepository workerRepository;
    private final DefinitionRepository definitionRepository;
    private final TaskInstanceRepository taskInstanceRepository;
    private final ParameterRepository parameterRepository;
    private final ParameterDomainService parameterDomainService;
    private final StorageService storageService;
    private final DockerWorker dockerWorker;
    private final WorkerDomainService workerDomainService;
    private final OutputParameterReferRepository outputParameterReferRepository;
    private final ReferenceRepository referenceRepository;
    private final ReferenceDomainService referenceDomainService;
    private final KVPairRepository kvPairRepository;

    @Inject
    public WorkerApplication(
            WorkerRepository workerRepository,
            DefinitionRepository definitionRepository,
            TaskInstanceRepository taskInstanceRepository,
            ParameterRepository parameterRepository,
            ParameterDomainService parameterDomainService,
            StorageService storageService,
            DockerWorker dockerWorker,
            WorkerDomainService workerDomainService,
            OutputParameterReferRepository outputParameterReferRepository,
            ReferenceRepository referenceRepository,
            ReferenceDomainService referenceDomainService,
            KVPairRepository kvPairRepository
    ) {
        this.workerRepository = workerRepository;
        this.definitionRepository = definitionRepository;
        this.taskInstanceRepository = taskInstanceRepository;
        this.parameterRepository = parameterRepository;
        this.parameterDomainService = parameterDomainService;
        this.storageService = storageService;
        this.dockerWorker = dockerWorker;
        this.workerDomainService = workerDomainService;
        this.outputParameterReferRepository = outputParameterReferRepository;
        this.referenceRepository = referenceRepository;
        this.referenceDomainService = referenceDomainService;
        this.kvPairRepository = kvPairRepository;
    }

    public void online(String workerId) {
        Worker worker = this.workerRepository.findById(workerId).orElseThrow(() -> new DataNotFoundException("未找到该Worker"));
        worker.online();
        this.workerRepository.updateStatus(worker);
    }

    public void offline(String workerId) {
        Worker worker = this.workerRepository.findById(workerId).orElseThrow(() -> new DataNotFoundException("未找到该Worker"));
        worker.offline();
        this.workerRepository.updateStatus(worker);
    }

    public void createVolume(String volumeName) {
        this.dockerWorker.createVolume(volumeName);
    }

    public void deleteVolume(String volumeName) {
        this.dockerWorker.deleteVolume(volumeName);
    }

    public void runTask(TaskInstance taskInstance) {
        // 创建DockerTask
        var taskDefinition = this.definitionRepository
                .findByKey(taskInstance.getDefKey())
                .orElseThrow(() -> new DataNotFoundException("未找到该任务定义"));
        if (!(taskDefinition instanceof DockerDefinition)) {
            throw new RuntimeException("任务定义类型错误");
        }
        var environmentMap = this.getEnvironmentMap(taskDefinition, taskInstance);
        var dockerTask = this.workerDomainService
                .createDockerTask((DockerDefinition) taskDefinition, taskInstance, environmentMap);
        // 创建logWriter
        var logWriter = this.storageService.writeLog(taskInstance.getId());
        // 执行任务
        this.dockerWorker.runTask(dockerTask, logWriter);
    }

    private Parameter<?> findSecret(Parameter<?> parameter) {
        // 处理密钥类型参数, 获取值后转换为String类型参数
        var strings = parameter.getStringValue().split("\\.");
        var kv = this.kvPairRepository
                .findByNamespaceNameAndKey(strings[0], strings[1])
                .orElseThrow(() -> new DataNotFoundException("未找到密钥"));
        return Parameter.Type.STRING.newParameter(kv.getValue());
    }

    private Map<String, String> getEnvironmentMap(Definition definition, TaskInstance taskInstance) {
        // 获得输入参数原始Map
        var parameterMap = definition.getInputParameters().stream()
                .map(taskParameter -> Map.entry(
                        "JIANMU_" + taskParameter.getRef().toUpperCase(),
                        taskParameter.getParameterId()
                        )
                )
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        // 根据ContextId查询输入参数引用
        var references = this.referenceRepository
                .findByContextIds(
                        Set.of(
                                // 使用TriggerId + AsyncTaskRef为参数引用 ContextId, 触发器参数覆盖输入参数场景
                                // 参见DSL导入创建参数引用逻辑,DslApplication#createRefs
                                taskInstance.getTriggerId() + taskInstance.getAsyncTaskRef()
                        )
                );
        references.forEach(reference -> logger.info("-------------reference parameterId--------: {}", reference.getParameterId()));
        // 计算新输入参数Map
        var newParameterMap = this.referenceDomainService
                .calculateIds(parameterMap, references);

        // 根据ContextId查询输出参数引用
        var outputParameterRefers = this.outputParameterReferRepository.findByContextId(taskInstance.getTriggerId() + taskInstance.getAsyncTaskRef());
        outputParameterRefers.forEach(outputParameterRefer -> {
            logger.info("outputParameterRefer: {}", outputParameterRefer);
            // 查询最后一次执行的任务实例
            this.taskInstanceRepository
                    .limitByAsyncTaskRefAndBusinessId(outputParameterRefer.getOutputNodeType(), taskInstance.getBusinessId())
                    .flatMap(i -> i.getOutputParameters().stream()
                            // 查找与输出参数引用关系匹配的输出参数
                            .filter(taskParameter -> taskParameter.getRef().equals(outputParameterRefer.getOutputParameterRef()))
                            .findFirst())
                    // 如果存在，覆盖输入参数Map中的value(ParameterId)
                    .ifPresent(taskParameter -> newParameterMap.put(taskParameter.getRef(), taskParameter.getParameterId()));
        });

        // 根据参数ID去参数上下文查询参数值， 如果是密钥类型参数则去密钥管理上下文获取值
        var parameters = this.parameterRepository
                .findByIds(new HashSet<>(newParameterMap.values()))
                .stream()
                .map(parameter -> {
                    // 处理密钥类型参数
                    if (parameter instanceof SecretParameter) {
                        return this.findSecret(parameter);
                    }
                    return parameter;
                })
                .collect(Collectors.toList());
        // 返回输入参数列表
        return this.parameterDomainService.createParameterMap(newParameterMap, parameters)
                .entrySet().stream()
                // 不同参数类型都转换为String传递
                .map(entry -> Map.entry(entry.getKey(), entry.getValue().getStringValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
