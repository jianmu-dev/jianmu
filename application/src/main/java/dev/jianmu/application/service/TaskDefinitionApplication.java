package dev.jianmu.application.service;

import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.infrastructure.client.RegistryClient;
import dev.jianmu.task.aggregate.Definition;
import dev.jianmu.task.aggregate.DockerDefinition;
import dev.jianmu.task.aggregate.TaskParameter;
import dev.jianmu.task.repository.DefinitionRepository;
import dev.jianmu.workflow.aggregate.parameter.Parameter;
import dev.jianmu.workflow.repository.ParameterRepository;
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
    private final ParameterRepository parameterRepository;
    private final RegistryClient registryClient;

    @Inject
    public TaskDefinitionApplication(
            DefinitionRepository definitionRepository,
            ParameterRepository parameterRepository,
            RegistryClient registryClient
    ) {
        this.definitionRepository = definitionRepository;
        this.parameterRepository = parameterRepository;
        this.registryClient = registryClient;
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
    public void createDockerDefinition(DockerDefinition dockerDefinition) {
        // 创建参数存储
        var parameters = this.mergeParameters(dockerDefinition.getInputParameters(), dockerDefinition.getOutputParameters());
        // 保存
        this.parameterRepository.addAll(parameters);
        this.definitionRepository.add(dockerDefinition);
    }

    @Transactional
    public void installDefinition(String ref, String version) {
        var dockerDefinition = this.registryClient.findByRefAndVersion(ref, version)
                .filter(definition -> definition instanceof DockerDefinition)
                .map(definition -> (DockerDefinition) definition)
                .orElseThrow(() -> new DataNotFoundException("未找到该组件定义"));
        // 创建参数存储
        var parameters = this.mergeParameters(dockerDefinition.getInputParameters(), dockerDefinition.getOutputParameters());
        // 保存
        this.parameterRepository.addAll(parameters);
        this.definitionRepository.add(dockerDefinition);
    }

    @Transactional
    public void installDefinitions(List<Definition> definitions) {
        // 创建参数存储
        var parameters = definitions.stream()
                .filter(definition -> definition instanceof DockerDefinition)
                .map(definition -> (DockerDefinition) definition)
                .map(dockerDefinition -> this.mergeParameters(dockerDefinition.getInputParameters(), dockerDefinition.getOutputParameters()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        // 批量保存
        this.parameterRepository.addAll(parameters);
        this.definitionRepository.addAll(definitions);
    }
}
