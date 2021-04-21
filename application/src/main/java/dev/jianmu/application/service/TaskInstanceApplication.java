package dev.jianmu.application.service;

import dev.jianmu.task.aggregate.Definition;
import dev.jianmu.task.aggregate.InstanceStatus;
import dev.jianmu.task.aggregate.TaskInstance;
import dev.jianmu.task.repository.DefinitionRepository;
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
    private final DefinitionRepository definitionRepository;
    private final InstanceDomainService instanceDomainService;

    @Inject
    public TaskInstanceApplication(
            TaskInstanceRepository taskInstanceRepository,
            DefinitionRepository definitionRepository,
            InstanceDomainService instanceDomainService
    ) {
        this.taskInstanceRepository = taskInstanceRepository;
        this.definitionRepository = definitionRepository;
        this.instanceDomainService = instanceDomainService;
    }

    @Transactional
    public void create(String businessId, String triggerId, String taskRef) {
        // 创建任务实例
        Definition definition = this.definitionRepository.findByKey(taskRef)
                .orElseThrow(() -> new RuntimeException("未找到任务定义"));
        List<TaskInstance> taskInstances = this.taskInstanceRepository.findByDefKeyAndBusinessId(taskRef, businessId);
        TaskInstance taskInstance = this.instanceDomainService.create(taskInstances, definition, businessId, triggerId);
        this.taskInstanceRepository.add(taskInstance);
    }

    @Transactional
    public void updateStatus(String taskInstanceId, InstanceStatus status) {
        TaskInstance taskInstance = this.taskInstanceRepository.findById(taskInstanceId)
                .orElseThrow(() -> new RuntimeException("未找到该任务实例"));
        // TODO 需要更新任务结束时间
        taskInstance.setStatus(status);
        this.taskInstanceRepository.updateStatus(taskInstance);
    }
}
