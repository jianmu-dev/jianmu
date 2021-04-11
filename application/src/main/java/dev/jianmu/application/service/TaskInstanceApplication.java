package dev.jianmu.application.service;

import dev.jianmu.parameter.repository.ParameterRepository;
import dev.jianmu.parameter.repository.ReferenceRepository;
import dev.jianmu.parameter.service.ParameterDomainService;
import dev.jianmu.parameter.service.ReferenceDomainService;
import dev.jianmu.task.aggregate.InstanceStatus;
import dev.jianmu.task.aggregate.TaskDefinition;
import dev.jianmu.task.aggregate.TaskInstance;
import dev.jianmu.task.repository.TaskDefinitionRepository;
import dev.jianmu.task.repository.TaskInstanceRepository;
import dev.jianmu.task.service.InstanceDomainService;
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
 * @class: TaskInstanceApplication
 * @description: 任务实例门面类
 * @author: Ethan Liu
 * @create: 2021-03-25 20:33
 **/
@Service
public class TaskInstanceApplication {
    private static final Logger logger = LoggerFactory.getLogger(TaskInstanceApplication.class);

    private final TaskInstanceRepository taskInstanceRepository;
    private final TaskDefinitionRepository taskDefinitionRepository;
    private final InstanceDomainService instanceDomainService;
    private final ReferenceRepository referenceRepository;
    private final ParameterRepository parameterRepository;
    private final ReferenceDomainService referenceDomainService;
    private final ParameterDomainService parameterDomainService;

    @Inject
    public TaskInstanceApplication(
            TaskInstanceRepository taskInstanceRepository,
            TaskDefinitionRepository taskDefinitionRepository,
            InstanceDomainService instanceDomainService,
            ReferenceRepository referenceRepository,
            ParameterRepository parameterRepository,
            ReferenceDomainService referenceDomainService,
            ParameterDomainService parameterDomainService
    ) {
        this.taskInstanceRepository = taskInstanceRepository;
        this.taskDefinitionRepository = taskDefinitionRepository;
        this.instanceDomainService = instanceDomainService;
        this.referenceRepository = referenceRepository;
        this.parameterRepository = parameterRepository;
        this.referenceDomainService = referenceDomainService;
        this.parameterDomainService = parameterDomainService;
    }

    public Map<String, String> getEnvironmentMap(TaskInstance taskInstance) {
        var parameterMap = taskInstance.getParameters().stream()
                .map(taskParameter -> Map.entry(
                        "JIANMU_" + taskParameter.getRef().toUpperCase(),
                        taskParameter.getParameterId()
                        )
                )
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        var references = this.referenceRepository
                .findByContextId(taskInstance.getDefKey() + taskInstance.getDefVersion());
        var newParameterMap = this.referenceDomainService
                .calculateIds(parameterMap, references);
        var parameters = this.parameterRepository
                .findByIds(new HashSet<>(newParameterMap.values()));
        return this.parameterDomainService.createParameterMap(newParameterMap, parameters)
                .entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), (String) entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Transactional
    public void create(String businessId, String taskRef) {
        // 创建任务实例
        TaskDefinition taskDefinition = this.taskDefinitionRepository.findByKeyVersion(taskRef)
                .orElseThrow(() -> new RuntimeException("未找到任务定义"));
        List<TaskInstance> taskInstances = this.taskInstanceRepository.findByKeyVersionAndBusinessId(taskRef, businessId);
        TaskInstance taskInstance = this.instanceDomainService.create(taskInstances, taskDefinition, businessId);
        this.taskInstanceRepository.add(taskInstance);
    }

    @Transactional
    public void updateStatus(String taskInstanceId, InstanceStatus status) {
        TaskInstance taskInstance = this.taskInstanceRepository.findById(taskInstanceId)
                .orElseThrow(() -> new RuntimeException("未找到该任务实例"));
        taskInstance.setStatus(status);
        this.taskInstanceRepository.updateStatus(taskInstance);
    }
}
