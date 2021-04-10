package dev.jianmu.application.service;

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

    @Inject
    public TaskInstanceApplication(
            TaskInstanceRepository taskInstanceRepository,
            TaskDefinitionRepository taskDefinitionRepository,
            InstanceDomainService instanceDomainService
    ) {
        this.taskInstanceRepository = taskInstanceRepository;
        this.taskDefinitionRepository = taskDefinitionRepository;
        this.instanceDomainService = instanceDomainService;
    }

    @Transactional
    public void create(String businessId, String taskRef) {
        // 创建任务实例
        TaskDefinition taskDefinition = this.taskDefinitionRepository.findByKeyVersion(taskRef)
                .orElseThrow(() -> new RuntimeException("未找到任务定义"));
        List<TaskInstance> taskInstances = this.taskInstanceRepository.findByKeyVersionAndBusinessId(taskRef, businessId);
        TaskInstance taskInstance = this.instanceDomainService.create(taskInstances, taskDefinition, businessId);
        // TODO 基础层创建参数保存表
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
