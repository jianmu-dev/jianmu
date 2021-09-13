package dev.jianmu.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.application.query.NodeDef;
import dev.jianmu.application.query.NodeDefApi;
import dev.jianmu.embedded.worker.aggregate.DockerTask;
import dev.jianmu.embedded.worker.aggregate.DockerWorker;
import dev.jianmu.embedded.worker.aggregate.spec.ContainerSpec;
import dev.jianmu.embedded.worker.aggregate.spec.HostConfig;
import dev.jianmu.embedded.worker.aggregate.spec.Mount;
import dev.jianmu.embedded.worker.aggregate.spec.MountType;
import dev.jianmu.infrastructure.storage.StorageService;
import dev.jianmu.secret.aggregate.KVPair;
import dev.jianmu.secret.repository.KVPairRepository;
import dev.jianmu.task.aggregate.InstanceParameter;
import dev.jianmu.task.aggregate.TaskInstance;
import dev.jianmu.task.repository.InstanceParameterRepository;
import dev.jianmu.worker.aggregate.Worker;
import dev.jianmu.worker.aggregate.WorkerTask;
import dev.jianmu.worker.repository.WorkerRepository;
import dev.jianmu.workflow.aggregate.parameter.Parameter;
import dev.jianmu.workflow.aggregate.parameter.SecretParameter;
import dev.jianmu.workflow.repository.ParameterRepository;
import dev.jianmu.workflow.service.ParameterDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ParameterRepository parameterRepository;
    private final ParameterDomainService parameterDomainService;
    private final DockerWorker dockerWorker;
    private final NodeDefApi nodeDefApi;
    private final WorkerRepository workerRepository;
    private final ApplicationEventPublisher publisher;
    private final InstanceParameterRepository instanceParameterRepository;

    public WorkerApplication(
            ParameterRepository parameterRepository,
            ParameterDomainService parameterDomainService,
            DockerWorker dockerWorker,
            NodeDefApi nodeDefApi,
            WorkerRepository workerRepository,
            ApplicationEventPublisher publisher,
            InstanceParameterRepository instanceParameterRepository
    ) {
        this.parameterRepository = parameterRepository;
        this.parameterDomainService = parameterDomainService;
        this.dockerWorker = dockerWorker;
        this.nodeDefApi = nodeDefApi;
        this.workerRepository = workerRepository;
        this.publisher = publisher;
        this.instanceParameterRepository = instanceParameterRepository;
    }

    public void createVolume(String volumeName) {
        this.dockerWorker.createVolume(volumeName);
    }

    public void deleteVolume(String volumeName) {
        this.dockerWorker.deleteVolume(volumeName);
    }

    public void dispatchTask(TaskInstance taskInstance) {
        // 查找节点定义
        var nodeDef = this.nodeDefApi.findByType(taskInstance.getDefKey());
        if (!nodeDef.getWorkerType().equals("DOCKER")) {
            throw new RuntimeException("无法执行此类节点任务: " + nodeDef.getType());
        }
        // 查找空闲Worker
        // TODO 暂时全部分配给内置Worker
        var worker = this.workerRepository.findByType(Worker.Type.EMBEDDED);
        // 创建WorkerTask
        var instanceParameters = this.instanceParameterRepository
                .findByInstanceIdAndType(taskInstance.getId(), InstanceParameter.Type.INPUT);
        var parameterMap = this.getParameterMap(instanceParameters);
        var workerTask = WorkerTask.Builder.aWorkerTask()
                .workerId(worker.getId())
                .type(worker.getType())
                .taskInstanceId(taskInstance.getId())
                .businessId(taskInstance.getBusinessId())
                .triggerId(taskInstance.getTriggerId())
                .defKey(taskInstance.getDefKey())
                .resultFile(nodeDef.getResultFile())
                .spec(nodeDef.getSpec())
                .parameterMap(parameterMap)
                .build();
        // 发送给Worker执行
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
        // 替换实际参数值
        this.parameterDomainService.createParameterMap(parameterMap, parameters);
        return parameterMap;
    }
}
