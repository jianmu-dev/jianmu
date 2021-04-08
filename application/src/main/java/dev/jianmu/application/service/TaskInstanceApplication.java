package dev.jianmu.application.service;

import dev.jianmu.parameter.aggregate.ParameterDefinition;
import dev.jianmu.parameter.aggregate.ParameterInstance;
import dev.jianmu.parameter.repository.ParameterDefinitionRepository;
import dev.jianmu.parameter.repository.ParameterInstanceRepository;
import dev.jianmu.parameter.service.ParameterDomainService;
import dev.jianmu.task.aggregate.TaskDefinition;
import dev.jianmu.task.aggregate.TaskInstance;
import dev.jianmu.task.aggregate.InstanceStatus;
import dev.jianmu.task.repository.TaskDefinitionRepository;
import dev.jianmu.task.repository.TaskInstanceRepository;
import dev.jianmu.task.service.InstanceDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

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
    private final ParameterDefinitionRepository parameterDefinitionRepository;
    private final ParameterInstanceRepository parameterInstanceRepository;
    private final ParameterDomainService parameterDomainService;

    @Inject
    public TaskInstanceApplication(TaskInstanceRepository taskInstanceRepository, TaskDefinitionRepository taskDefinitionRepository, InstanceDomainService instanceDomainService, WorkflowInstanceApplication workflowInstanceApplication, ParameterDefinitionRepository parameterDefinitionRepository, ParameterInstanceRepository parameterInstanceRepository, ParameterDomainService parameterDomainService) {
        this.taskInstanceRepository = taskInstanceRepository;
        this.taskDefinitionRepository = taskDefinitionRepository;
        this.instanceDomainService = instanceDomainService;
        this.parameterDefinitionRepository = parameterDefinitionRepository;
        this.parameterInstanceRepository = parameterInstanceRepository;
        this.parameterDomainService = parameterDomainService;
    }

    @Transactional
    public void create(String businessId, String taskRef) {
        // 创建任务实例
        TaskDefinition taskDefinition = this.taskDefinitionRepository.findByKeyVersion(taskRef)
                .orElseThrow(() -> new RuntimeException("未找到任务定义"));
        List<TaskInstance> taskInstances = this.taskInstanceRepository.findByKeyVersionAndBusinessId(taskRef, businessId);
        TaskInstance taskInstance = this.instanceDomainService.create(taskInstances, taskDefinition, businessId);
        // 创建任务实例参数
        var taskInputDefinitions = this.parameterDefinitionRepository
                .findByBusinessIdAndScope(taskRef, "TaskInput");
        var parameterInstances = this.parameterDomainService
                .createTaskInputParameterInstance(taskInstance.getId(), taskInputDefinitions);
        // 保存
        this.taskInstanceRepository.add(taskInstance);
        this.parameterInstanceRepository.addList(parameterInstances);
    }

    @Transactional
    public void updateStatus(String taskInstanceId, InstanceStatus status) {
        TaskInstance taskInstance = this.taskInstanceRepository.findById(taskInstanceId)
                .orElseThrow(() -> new RuntimeException("未找到该任务实例"));
        taskInstance.setStatus(status);
        this.taskInstanceRepository.updateStatus(taskInstance);
    }
}
