package dev.jianmu.application.service;

import dev.jianmu.parameter.aggregate.ParameterDefinition;
import dev.jianmu.parameter.repository.ParameterDefinitionRepository;
import dev.jianmu.task.aggregate.TaskDefinition;
import dev.jianmu.task.repository.TaskDefinitionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

/**
 * @class: TaskDefinitionApplication
 * @description: 任务定义门面类
 * @author: Ethan Liu
 * @create: 2021-03-25 20:31
 **/
@Service
public class TaskDefinitionApplication {
    private static final Logger logger = LoggerFactory.getLogger(TaskDefinitionApplication.class);
    private final TaskDefinitionRepository taskDefinitionRepository;
    private final ParameterDefinitionRepository parameterDefinitionRepository;

    @Inject
    public TaskDefinitionApplication(TaskDefinitionRepository taskDefinitionRepository, ParameterDefinitionRepository parameterDefinitionRepository) {
        this.taskDefinitionRepository = taskDefinitionRepository;
        this.parameterDefinitionRepository = parameterDefinitionRepository;
    }

    @Transactional
    public void create(TaskDefinition taskDefinition, List<ParameterDefinition<?>> parameterDefinitionList) {
        // 同步保存任务定义参数
        this.parameterDefinitionRepository.addList(parameterDefinitionList);
        this.taskDefinitionRepository.add(taskDefinition);
    }

    public Optional<TaskDefinition> findByKeyVersion(String key, String version) {
        return this.taskDefinitionRepository.findByKeyVersion(key + version);
    }

    public List<TaskDefinition> findAll() {
        return this.taskDefinitionRepository.findAll();
    }
}
