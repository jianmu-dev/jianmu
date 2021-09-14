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
import dev.jianmu.secret.aggregate.KVPair;
import dev.jianmu.secret.repository.KVPairRepository;
import dev.jianmu.worker.aggregate.WorkerTask;
import dev.jianmu.workflow.aggregate.parameter.Parameter;
import dev.jianmu.workflow.aggregate.parameter.SecretParameter;
import dev.jianmu.workflow.repository.ParameterRepository;
import dev.jianmu.workflow.service.ParameterDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final ParameterRepository parameterRepository;
    private final ParameterDomainService parameterDomainService;
    private final KVPairRepository kvPairRepository;
    private final StorageService storageService;
    private final DockerWorker dockerWorker;
    private final ObjectMapper objectMapper;

    public EmbeddedWorkerApplication(
            ParameterRepository parameterRepository,
            ParameterDomainService parameterDomainService,
            KVPairRepository kvPairRepository,
            StorageService storageService,
            DockerWorker dockerWorker,
            ObjectMapper objectMapper
    ) {
        this.parameterRepository = parameterRepository;
        this.parameterDomainService = parameterDomainService;
        this.kvPairRepository = kvPairRepository;
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
        try {
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
            parameterMap.put("JIANMU_SHARE_DIR", "/" + workerTask.getTriggerId());
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
        return this.kvPairRepository.findByNamespaceNameAndKey(strings[0], strings[1]);
    }
}
