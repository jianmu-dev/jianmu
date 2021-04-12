package dev.jianmu.parameter.service;

import dev.jianmu.parameter.aggregate.*;

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

    // TODO 未来需要在Parameter上支持多类型
    public Map<String, Parameter> createParameters(Map<String, Object> parameterMap) {
        return parameterMap.entrySet().stream()
                .map(entry -> {
                    if (entry.getValue() == null) {
                        throw new RuntimeException("当前不支持非String类型参数");
                    }
                    var p = Parameter.Builder.aParameter()
                            .type("String")
                            .value((String) entry.getValue())
                            .build();
                    return Map.entry(entry.getKey(), p);
                }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, String> createParameterMap(Map<String, Parameter> parameterMap) {
        return parameterMap.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), entry.getValue().getId()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, Object> createParameterMap(Map<String, String> parameterMap, List<Parameter> parameters) {
        return parameterMap.entrySet().stream().map(entry -> {
            var realValue = parameters.stream()
                    .filter(parameter -> parameter.getId().equals(entry.getValue()))
                    .findFirst()
                    .orElseThrow();
            return Map.entry(entry.getKey(), realValue.getValue());
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
