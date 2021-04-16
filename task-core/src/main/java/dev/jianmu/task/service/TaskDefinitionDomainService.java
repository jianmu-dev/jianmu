package dev.jianmu.task.service;

import dev.jianmu.task.aggregate.BaseTaskDefinition;
import dev.jianmu.task.aggregate.TaskDefinition;
import dev.jianmu.task.aggregate.TaskParameter;

import java.util.Map;
import java.util.Set;

/**
 * @class: TaskDefinitionDomainService
 * @description: 任务定义Domain服务
 * @author: Ethan Liu
 * @create: 2021-04-10 19:57
 **/
public class TaskDefinitionDomainService {

    public void handleParameterMap(
            TaskDefinition taskDefinition,
            Set<TaskParameter> taskParameters,
            Map<String, String> parameterMap
    ) {
        taskParameters.forEach(taskParameter -> {
            var parameterId = parameterMap.get(taskParameter.getRef());
            taskParameter.setParameterId(parameterId);
        });
        taskDefinition.setParameters(taskParameters);
    }
}
