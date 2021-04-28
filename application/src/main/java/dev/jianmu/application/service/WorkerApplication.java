package dev.jianmu.application.service;

import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.dsl.repository.OutputParameterReferRepository;
import dev.jianmu.infrastructure.storage.StorageService;
import dev.jianmu.parameter.aggregate.Parameter;
import dev.jianmu.parameter.aggregate.SecretParameter;
import dev.jianmu.parameter.repository.ParameterRepository;
import dev.jianmu.parameter.repository.ReferenceRepository;
import dev.jianmu.parameter.service.ParameterDomainService;
import dev.jianmu.parameter.service.ReferenceDomainService;
import dev.jianmu.secret.repository.KVPairRepository;
import dev.jianmu.task.aggregate.*;
import dev.jianmu.task.repository.DefinitionRepository;
import dev.jianmu.task.repository.InputParameterRepository;
import dev.jianmu.task.repository.TaskInstanceRepository;
import dev.jianmu.task.repository.WorkerRepository;
import dev.jianmu.task.service.WorkerDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private final InputParameterRepository inputParameterRepository;

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
            KVPairRepository kvPairRepository,
            InputParameterRepository inputParameterRepository
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
        this.inputParameterRepository = inputParameterRepository;
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
        var inputParameters = this.inputParameterRepository
                .findByBusinessIdAndAsyncTaskRef(taskInstance.getBusinessId(), taskInstance.getAsyncTaskRef());
        var taskInputParameters = definition.getInputParametersWith(inputParameters);
        // 获得输入参数原始Map
        var parameterMap = taskInputParameters.stream()
                .map(taskParameter -> Map.entry(
                        "JIANMU_" + taskParameter.getRef().toUpperCase(),
                        taskParameter.getParameterId()
                        )
                )
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        // 根据参数ID去参数上下文查询参数值， 如果是密钥类型参数则去密钥管理上下文获取值
        var parameters = this.parameterRepository.findByIds(new HashSet<>(parameterMap.values()));
        var secretParameters = parameters.stream()
                .filter(parameter -> parameter instanceof SecretParameter)
                .collect(Collectors.toList());
        this.handleSecretParameter(parameterMap, secretParameters);
        this.parameterDomainService.createParameterMap(parameterMap, parameters);

        // 返回输入参数列表
        return parameterMap;
    }

    private void handleSecretParameter(Map<String, String> parameterMap, List<Parameter> secretParameters) {
        parameterMap.forEach((key, val) -> {
            secretParameters.stream()
                    .filter(parameter -> parameter.getId().equals(val))
                    .findFirst()
                    .ifPresent(parameter -> {
                        var secretParameter = this.findSecret(parameter);
                        parameterMap.put(key, secretParameter.getStringValue());
                    });
        });
    }
}
