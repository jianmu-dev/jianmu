package dev.jianmu.task.service;

import dev.jianmu.task.aggregate.Definition;
import dev.jianmu.task.aggregate.DockerDefinition;
import dev.jianmu.task.aggregate.TaskParameter;
import dev.jianmu.task.aggregate.spec.ContainerSpec;

import java.util.Map;
import java.util.Set;

/**
 * @class: TaskDefinitionDomainService
 * @description: 任务定义Domain服务
 * @author: Ethan Liu
 * @create: 2021-04-10 19:57
 **/
public class DefinitionDomainService {

    public Definition createDockerDefinition(
            String key,
            String resultFile,
            Set<TaskParameter> inputParameters,
            Set<TaskParameter> outputParameters,
            ContainerSpec spec
    ) {
        return DockerDefinition.Builder.aDockerDefinition()
                .key(key)
                .resultFile(resultFile)
                .inputParameters(inputParameters)
                .outputParameters(outputParameters)
                .spec(spec)
                .build();
    }
}
