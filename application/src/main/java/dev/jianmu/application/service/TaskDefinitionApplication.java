package dev.jianmu.application.service;

import com.github.pagehelper.PageInfo;
import dev.jianmu.application.exception.DataNotFoundException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private List<Parameter> createParameters(Set<TaskParameter> parameters) {
        var parameterMap = parameters.stream()
                .map(taskParameter ->
                        Map.entry(
                                taskParameter,
                                Parameter.Type
                                        .valueOf(taskParameter.getType())
                                        .newParameter(taskParameter.getValue())
                        )
                )
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        parameters.forEach(taskParameter -> {
            var parameterId = parameterMap.get(taskParameter).getId();
            taskParameter.setParameterId(parameterId);
        });
        return new ArrayList<>(parameterMap.values());
    }

    private List<Parameter> mergeParameters(Set<TaskParameter> inputParameters, Set<TaskParameter> outputParameters) {
        // 创建参数存储
        var inParameters = this.createParameters(inputParameters);
        var outParameters = this.createParameters(outputParameters);
        inParameters.addAll(outParameters);
        return inParameters;
    }

    @Transactional
    public void createDockerDefinition(
            TaskDefinition taskDefinition,
            TaskDefinitionVersion taskDefinitionVersion,
            String resultFile,
            Set<TaskParameter> inputParameters,
            Set<TaskParameter> outputParameters,
            ContainerSpec spec
    ) {
        var definitionKey = taskDefinition.getRef() + taskDefinitionVersion.getName();
        taskDefinitionVersion.setTaskDefinitionId(taskDefinition.getId());
        taskDefinitionVersion.setDefinitionKey(definitionKey);
        // 创建参数存储
        var parameters = this.mergeParameters(inputParameters, outputParameters);
        // 生成definition
        var definition = this.definitionDomainService
                .createDockerDefinition(taskDefinitionVersion.getDefinitionKey(), resultFile, inputParameters, outputParameters, spec);
        // 保存
        this.taskDefinitionRepository.add(taskDefinition);
        this.taskDefinitionVersionRepository.add(taskDefinitionVersion);
        this.parameterRepository.addAll(parameters);
        this.definitionRepository.add(definition);
    }

    @Transactional
    public void createDockerDefinitionVersion(
            TaskDefinitionVersion taskDefinitionVersion,
            String resultFile,
            Set<TaskParameter> inputParameters,
            Set<TaskParameter> outputParameters,
            ContainerSpec spec
    ) {
        var taskDefinition = this.taskDefinitionRepository
                .findByRef(taskDefinitionVersion.getTaskDefinitionRef())
                .orElseThrow(() -> new DataNotFoundException("未找到该任务定义"));
        var definitionKey = taskDefinition.getRef() + taskDefinitionVersion.getName();
        taskDefinitionVersion.setDefinitionKey(definitionKey);
        taskDefinitionVersion.setTaskDefinitionId(taskDefinition.getId());
        // 创建参数存储
        var parameters = this.mergeParameters(inputParameters, outputParameters);
        // 生成definition
        var definition = this.definitionDomainService
                .createDockerDefinition(taskDefinitionVersion.getDefinitionKey(), resultFile, inputParameters, outputParameters, spec);
        // 保存
        this.taskDefinitionVersionRepository.add(taskDefinitionVersion);
        this.parameterRepository.addAll(parameters);
        this.definitionRepository.add(definition);
    }

    public Definition findByKey(String key) {
        return this.definitionRepository.findByKey(key).orElseThrow(() -> new DataNotFoundException("未找到该任务定义版本"));
    }

    public TaskDefinitionVersion findByRefAndName(String ref, String name) {
        return this.taskDefinitionVersionRepository.findByTaskDefinitionRefAndName(ref, name)
                .orElseThrow(() -> new DataNotFoundException("未找到该任务定义版本"));
    }

    public TaskDefinition findByRef(String ref) {
        return this.taskDefinitionRepository.findByRef(ref).orElseThrow(() -> new DataNotFoundException("未找到该任务定义"));
    }

    public List<TaskDefinitionVersion> findVersionByRef(String ref) {
        return this.taskDefinitionVersionRepository.findByTaskDefinitionRef(ref);
    }

    public PageInfo<TaskDefinition> findAll(String name, int pageNum, int pageSize) {
        return this.taskDefinitionRepository.findAll(name, pageNum, pageSize);
    }

    public void deleteTaskDefinitionVersion(String ref, String name) {
        var versions = this.taskDefinitionVersionRepository
                .findByTaskDefinitionRef(ref);
        if (versions.size() == 1) {
            this.taskDefinitionRepository
                    .findByRef(versions.get(0).getTaskDefinitionRef())
                    .ifPresent(this.taskDefinitionRepository::delete);
        }
        var version = versions.stream()
                .filter(v -> v.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException("未找到该任务定义版本"));
        this.taskDefinitionVersionRepository.delete(version);
        this.definitionRepository.delete(version.getDefinitionKey());
    }
}
