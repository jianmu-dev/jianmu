package dev.jianmu.application.service;

import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.task.aggregate.Definition;
import dev.jianmu.task.aggregate.TaskInstance;
import dev.jianmu.task.repository.DefinitionRepository;
import dev.jianmu.task.repository.TaskInstanceRepository;
import dev.jianmu.task.service.InstanceDomainService;
import dev.jianmu.version.aggregate.TaskDefinition;
import dev.jianmu.version.aggregate.TaskDefinitionVersion;
import dev.jianmu.version.repository.TaskDefinitionRepository;
import dev.jianmu.version.repository.TaskDefinitionVersionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

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
    private final TaskDefinitionRepository taskDefinitionRepository;
    private final TaskDefinitionVersionRepository taskDefinitionVersionRepository;

    @Inject
    public TaskInstanceApplication(
            TaskInstanceRepository taskInstanceRepository,
            DefinitionRepository definitionRepository,
            InstanceDomainService instanceDomainService,
            TaskDefinitionRepository taskDefinitionRepository,
            TaskDefinitionVersionRepository taskDefinitionVersionRepository
    ) {
        this.taskInstanceRepository = taskInstanceRepository;
        this.definitionRepository = definitionRepository;
        this.instanceDomainService = instanceDomainService;
        this.taskDefinitionRepository = taskDefinitionRepository;
        this.taskDefinitionVersionRepository = taskDefinitionVersionRepository;
    }

    public List<TaskInstance> findByBusinessId(String businessId) {
        return this.taskInstanceRepository.findByBusinessId(businessId);
    }

    public Optional<TaskDefinitionVersion> findByDefKey(String defKey) {
        return this.taskDefinitionVersionRepository.findByDefinitionKey(defKey);
    }

    public Optional<TaskDefinition> findByRef(String ref) {
        return this.taskDefinitionRepository.findByRef(ref);
    }

    @Transactional
    public void create(String businessId, String triggerId, String asyncTaskRef, String asyncTaskType) {
        // 创建任务实例
        Definition definition = this.definitionRepository.findByKey(asyncTaskType)
                .orElseThrow(() -> new DataNotFoundException("未找到任务定义"));
        List<TaskInstance> taskInstances = this.taskInstanceRepository.findByAsyncTaskRefAndBusinessId(asyncTaskRef, businessId);
        TaskInstance taskInstance = this.instanceDomainService.create(taskInstances, definition, businessId, triggerId, asyncTaskRef);
        this.taskInstanceRepository.add(taskInstance);
    }

    @Transactional
    public void executeSucceeded(String taskInstanceId) {
        TaskInstance taskInstance = this.taskInstanceRepository.findById(taskInstanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该任务实例"));
        taskInstance.executeSucceeded();
        this.taskInstanceRepository.updateStatus(taskInstance);
    }

    @Transactional
    public void executeFailed(String taskInstanceId) {
        TaskInstance taskInstance = this.taskInstanceRepository.findById(taskInstanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该任务实例"));
        taskInstance.executeFailed();
        this.taskInstanceRepository.updateStatus(taskInstance);
    }

    @Transactional
    public void running(String taskInstanceId) {
        TaskInstance taskInstance = this.taskInstanceRepository.findById(taskInstanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该任务实例"));
        taskInstance.running();
        this.taskInstanceRepository.updateStatus(taskInstance);
    }
}
