package dev.jianmu.task.service;

import dev.jianmu.task.aggregate.TaskDefinition;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @class: TaskDefinitionDomainService
 * @description: 任务定义Domain服务
 * @author: Ethan Liu
 * @create: 2021-04-10 19:57
 **/
public class TaskDefinitionDomainService {
    private static final String PREFIX = "JIANMU_";

    public void handleInputParameterMap(TaskDefinition taskDefinition, Map<String, String> inputParameterMap) {
        // TODO 是否需要添加任务定义校验逻辑
        var newMap = inputParameterMap.entrySet().stream()
                .map(entry -> Map.entry(PREFIX + entry.getKey(), entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        taskDefinition.setInputParameters(newMap);
    }
}
