package dev.jianmu.application.service;

import com.github.pagehelper.PageInfo;
import dev.jianmu.infrastructure.mybatis.version.TaskDefinitionRepositoryImpl;
import dev.jianmu.parameter.aggregate.Parameter;
import dev.jianmu.parameter.repository.ParameterRepository;
import dev.jianmu.parameter.service.ParameterDomainService;
import dev.jianmu.task.aggregate.Definition;
import dev.jianmu.task.aggregate.TaskParameter;
import dev.jianmu.task.aggregate.spec.ContainerSpec;
import dev.jianmu.task.repository.DefinitionRepository;
import dev.jianmu.task.service.DefinitionDomainService;
import dev.jianmu.version.aggregate.TaskDefinition;
import dev.jianmu.version.aggregate.TaskDefinitionVersion;
import dev.jianmu.version.repository.TaskDefinitionVersionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.*;
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
    private final DefinitionRepository definitionRepository;
    private final ParameterDomainService parameterDomainService;
    private final ParameterRepository parameterRepository;
    private final TaskDefinitionRepositoryImpl taskDefinitionRepository;
    private final TaskDefinitionVersionRepository taskDefinitionVersionRepository;
    private final DefinitionDomainService definitionDomainService;

    @Inject
    public TaskDefinitionApplication(
            DefinitionRepository definitionRepository,
            ParameterDomainService parameterDomainService,
            ParameterRepository parameterRepository,
            TaskDefinitionRepositoryImpl taskDefinitionRepository,
            TaskDefinitionVersionRepository taskDefinitionVersionRepository,
            DefinitionDomainService definitionDomainService
    ) {
        this.definitionRepository = definitionRepository;
        this.parameterDomainService = parameterDomainService;
        this.parameterRepository = parameterRepository;
        this.taskDefinitionRepository = taskDefinitionRepository;
        this.taskDefinitionVersionRepository = taskDefinitionVersionRepository;
        this.definitionDomainService = definitionDomainService;
    }

    @Transactional
    public void createDockerDefinition(
            String name, String ref, String version, String resultFile, String description, Set<TaskParameter> taskParameters, ContainerSpec spec
    ) {
        var definitionKey = ref + version;
        var taskDefinition = TaskDefinition.Builder.aTaskDefinition().name(name).ref(ref).build();
        var definitionVersion = TaskDefinitionVersion.Builder.aTaskDefinitionVersion()
                .taskDefinitionId(taskDefinition.getId())
                .name(version)
                .taskDefinitionRef(ref)
                .definitionKey(definitionKey)
                .description(description)
                .build();
        // 创建参数存储
        var parameters = taskParameters.stream()
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
        // 生成definition
        var definition = this.definitionDomainService
                .createDockerDefinition(definitionVersion.getDefinitionKey(), resultFile, taskParameters, spec, parameterMap);
        // 保存
        this.parameterRepository.addAll(new ArrayList<>(parameters.values()));
        this.taskDefinitionRepository.add(taskDefinition);
        this.taskDefinitionVersionRepository.add(definitionVersion);
        this.definitionRepository.add(definition);
    }

    @Transactional
    public void createDockerDefinitionVersion(
            String ref, String version, String resultFile, String description, Set<TaskParameter> taskParameters, ContainerSpec spec
    ) {
        var taskDefinition = this.taskDefinitionRepository
                .findByRef(ref)
                .orElseThrow(() -> new RuntimeException("未找到该任务定义"));
        var definitionKey = ref + version;
        var definitionVersion = TaskDefinitionVersion.Builder.aTaskDefinitionVersion()
                .taskDefinitionId(taskDefinition.getId())
                .name(version)
                .taskDefinitionRef(ref)
                .definitionKey(definitionKey)
                .description(description)
                .build();
        // 创建参数存储
        var parameters = taskParameters.stream()
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
        // 生成definition
        var definition = this.definitionDomainService
                .createDockerDefinition(definitionVersion.getDefinitionKey(), resultFile, taskParameters, spec, parameterMap);
        // 保存
        this.parameterRepository.addAll(new ArrayList<>(parameters.values()));
        this.taskDefinitionVersionRepository.add(definitionVersion);
        this.definitionRepository.add(definition);
    }

    public Optional<Definition> findByKey(String key) {
        return this.definitionRepository.findByKey(key);
    }

    public Optional<TaskDefinition> findByRef(String ref) {
        return this.taskDefinitionRepository.findByRef(ref);
    }

    public List<TaskDefinitionVersion> findVersionByRef(String ref) {
        return this.taskDefinitionVersionRepository.findByTaskDefinitionRef(ref);
    }

    public PageInfo<TaskDefinition> findAll(int pageNum, int pageSize) {
        return this.taskDefinitionRepository.findAll(pageNum, pageSize);
    }
}
