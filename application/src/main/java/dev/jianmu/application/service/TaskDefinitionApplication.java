package dev.jianmu.application.service;

import dev.jianmu.parameter.aggregate.Parameter;
import dev.jianmu.parameter.repository.ParameterRepository;
import dev.jianmu.parameter.service.ParameterDomainService;
import dev.jianmu.task.aggregate.TaskDefinition;
import dev.jianmu.task.repository.TaskDefinitionRepository;
import dev.jianmu.task.service.TaskDefinitionDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final ParameterDomainService parameterDomainService;
    private final ParameterRepository parameterRepository;
    private final TaskDefinitionDomainService taskDefinitionDomainService;

    @Inject
    public TaskDefinitionApplication(
            TaskDefinitionRepository taskDefinitionRepository,
            ParameterDomainService parameterDomainService,
            ParameterRepository parameterRepository,
            TaskDefinitionDomainService taskDefinitionDomainService) {
        this.taskDefinitionRepository = taskDefinitionRepository;
        this.parameterDomainService = parameterDomainService;
        this.parameterRepository = parameterRepository;
        this.taskDefinitionDomainService = taskDefinitionDomainService;
    }

    @Transactional
    public void create(TaskDefinition taskDefinition) {
        // 创建参数存储
        var parameters = taskDefinition.getParameters().stream()
                .map(taskParameter ->
                        Map.entry(
                                taskParameter.getRef(),
                                Parameter.Builder.aParameter()
                                        .type(taskParameter.getType())
                                        .value(taskParameter.getValue())
                                        .build()
                        )
                )
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        // 转换为参数ref与参数id Map
        var parameterMap = this.parameterDomainService.createParameterMap(parameters);
        // 处理输入参数Map
        this.taskDefinitionDomainService.handleParameterMap(taskDefinition, taskDefinition.getParameters(), parameterMap);
        // 保存
        this.parameterRepository.addAll(new ArrayList<>(parameters.values()));
        this.taskDefinitionRepository.add(taskDefinition);
    }

    public Optional<TaskDefinition> findByKeyVersion(String key, String version) {
        return this.taskDefinitionRepository.findByKeyVersion(key + version);
    }

    public List<TaskDefinition> findAll() {
        return this.taskDefinitionRepository.findAll();
    }
}
