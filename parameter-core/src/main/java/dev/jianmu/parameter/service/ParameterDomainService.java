package dev.jianmu.parameter.service;

import dev.jianmu.parameter.aggregate.ParameterDefinition;
import dev.jianmu.parameter.aggregate.ParameterInstance;
import dev.jianmu.parameter.aggregate.StringParameterDefinition;
import dev.jianmu.parameter.aggregate.StringParameterInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @class: ParameterDomainService
 * @description: 参数领域服务
 * @author: Ethan Liu
 * @create: 2021-04-08 10:00
 **/
public class ParameterDomainService {

    private static final String ParameterPrefix = "JIANMU_";

    public List<ParameterInstance<?>> createTaskInputParameterInstance(
            String businessId,
            List<? extends ParameterDefinition<?>> taskDefinitionParameters
    ) {
        // TODO 未来需要实现触发器覆盖逻辑
        return taskDefinitionParameters.stream().map(definition -> {
            if (definition instanceof StringParameterDefinition) {
                return StringParameterInstance.Builder.aStringParameterInstance()
                        .name(definition.getName())
                        .ref(definition.getRef())
                        .description(definition.getDescription())
                        .businessId(businessId)
                        .type(definition.getType())
                        .scope(definition.getScope())
                        .value(((StringParameterDefinition) definition).getValue())
                        .build();
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    // 合并系统参数
    public Map<String, String> mergeSystemParameterMap(
            List<? extends ParameterDefinition<?>> workerParameters,
            List<? extends ParameterInstance<?>> taskInstanceParameters
    ) {
        Map<String, String> systemParameterMap = new HashMap<>();
        // 系统参数转Map
        workerParameters.forEach(parameterDefinition -> {
            if (parameterDefinition instanceof StringParameterDefinition) {
                var val = ((StringParameterDefinition) parameterDefinition).getValue();
                systemParameterMap.put(parameterDefinition.getRef(), val);
            }
        });
        // 合并系统参数
        taskInstanceParameters.forEach(parameterInstance -> {
            if (parameterInstance instanceof StringParameterInstance) {
                var val = ((StringParameterInstance) parameterInstance).getValue();
                systemParameterMap.computeIfPresent(parameterInstance.getRef(), (k, v) -> val);
            }
        });
        // 过滤空值
        systemParameterMap.values().removeIf(String::isBlank);

        return systemParameterMap;
    }

    // 合并业务参数
    public Map<String, String> mergeBusinessParameterMap(
            List<? extends ParameterDefinition<?>> workerParameters,
            List<? extends ParameterInstance<?>> taskInstanceParameters
    ) {
        Map<String, String> systemParameterMap = new HashMap<>();
        Map<String, String> businessParameterMap = new HashMap<>();
        workerParameters.forEach(parameterDefinition -> {
            if (parameterDefinition instanceof StringParameterDefinition) {
                systemParameterMap.put(parameterDefinition.getRef(), ((StringParameterDefinition) parameterDefinition).getValue());
            }
        });
        taskInstanceParameters.forEach(parameterInstance -> {
            if (parameterInstance instanceof StringParameterInstance) {
                var v = systemParameterMap.get(parameterInstance.getRef());
                if (v == null) {
                    businessParameterMap.put(
                            ParameterPrefix + parameterInstance.getRef().toUpperCase(),
                            ((StringParameterInstance) parameterInstance).getValue()
                    );
                }
            }
        });
        return businessParameterMap;
    }
}
