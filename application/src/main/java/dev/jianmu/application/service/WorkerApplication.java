package dev.jianmu.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.query.NodeDefApi;
import dev.jianmu.infrastructure.storage.StorageService;
import dev.jianmu.secret.aggregate.KVPair;
import dev.jianmu.secret.repository.KVPairRepository;
import dev.jianmu.task.aggregate.*;
import dev.jianmu.task.aggregate.spec.ContainerSpec;
import dev.jianmu.task.repository.InstanceParameterRepository;
import dev.jianmu.task.repository.WorkerRepository;
import dev.jianmu.task.service.WorkerDomainService;
import dev.jianmu.workflow.aggregate.parameter.Parameter;
import dev.jianmu.workflow.aggregate.parameter.SecretParameter;
import dev.jianmu.workflow.repository.ParameterRepository;
import dev.jianmu.workflow.service.ParameterDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final ParameterRepository parameterRepository;
    private final ParameterDomainService parameterDomainService;
    private final StorageService storageService;
    private final DockerWorker dockerWorker;
    private final WorkerDomainService workerDomainService;
    private final KVPairRepository kvPairRepository;
    private final NodeDefApi nodeDefApi;
    private final ObjectMapper objectMapper;
    private final InstanceParameterRepository instanceParameterRepository;

    public WorkerApplication(
            WorkerRepository workerRepository,
            ParameterRepository parameterRepository,
            ParameterDomainService parameterDomainService,
            StorageService storageService,
            DockerWorker dockerWorker,
            WorkerDomainService workerDomainService,
            KVPairRepository kvPairRepository,
            NodeDefApi nodeDefApi,
            ObjectMapper objectMapper,
            InstanceParameterRepository instanceParameterRepository
    ) {
        this.workerRepository = workerRepository;
        this.parameterRepository = parameterRepository;
        this.parameterDomainService = parameterDomainService;
        this.storageService = storageService;
        this.dockerWorker = dockerWorker;
        this.workerDomainService = workerDomainService;
        this.kvPairRepository = kvPairRepository;
        this.nodeDefApi = nodeDefApi;
        this.objectMapper = objectMapper;
        this.instanceParameterRepository = instanceParameterRepository;
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
        var nodeDef = this.nodeDefApi.findByType(taskInstance.getDefKey());
        if (!nodeDef.getWorkerType().equals("DOCKER")) {
            throw new RuntimeException("无法执行此类节点任务: " + nodeDef.getType());
        }
        try {
            var spec = objectMapper.readValue(nodeDef.getSpec(), ContainerSpec.class);
            var taskDefinition = DockerDefinition.Builder.aDockerDefinition()
                    .spec(spec)
                    .resultFile(nodeDef.getResultFile())
                    .build();
            var instanceParameters = this.instanceParameterRepository
                    .findByInstanceIdAndType(taskInstance.getId(), InstanceParameter.Type.INPUT);
            var environmentMap = this.getEnvironmentMap(instanceParameters);
            environmentMap.put("JIANMU_SHARE_DIR", "/" + taskInstance.getTriggerId());
            var dockerTask = this.workerDomainService
                    .createDockerTask(taskDefinition, taskInstance, environmentMap);
            // 创建logWriter
            var logWriter = this.storageService.writeLog(taskInstance.getId());
            // 执行任务
            this.dockerWorker.runTask(dockerTask, logWriter);
        } catch (RuntimeException | JsonProcessingException e) {
            logger.error("任务执行失败：", e);
            throw new RuntimeException("任务执行失败");
        }
    }

    private Optional<KVPair> findSecret(Parameter<?> parameter) {
        Parameter<?> secretParameter;
        // 处理密钥类型参数, 获取值后转换为String类型参数
        var strings = parameter.getStringValue().split("\\.");
        return this.kvPairRepository.findByNamespaceNameAndKey(strings[0], strings[1]);
    }

    private Map<String, String> getEnvironmentMap(List<InstanceParameter> instanceParameters) {
        // 创建任务输入参数Map,此时Map value为参数ID
        var parameterMap = instanceParameters.stream()
                .map(instanceParameter -> Map.entry(
                        "JIANMU_" + instanceParameter.getRef().toUpperCase(),
                        instanceParameter.getParameterId()
                        )
                )
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        // 根据参数ID去参数上下文查询参数值， 如果是密钥类型参数则去密钥管理上下文获取值
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

        // 返回处理后的输入参数Map
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
}
