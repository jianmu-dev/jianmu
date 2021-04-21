package dev.jianmu.parameter.service;

import dev.jianmu.parameter.aggregate.Parameter;
import dev.jianmu.parameter.aggregate.StringParameter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @class: ParameterDomainService
 * @description: 参数领域服务
 * @author: Ethan Liu
 * @create: 2021-04-08 10:00
 **/
public class ParameterDomainService {

    public Map<String, String> createParameterMap(Map<String, ? extends Parameter> parameterMap) {
        return parameterMap.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), entry.getValue().getId()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, Object> createParameterMap(Map<String, String> parameterMap, List<Parameter> parameters) {
        return parameterMap.entrySet().stream().map(entry -> {
            var realValue = parameters.stream()
                    .filter(parameter -> parameter.getId().equals(entry.getValue()))
                    .findFirst()
                    .orElse(new StringParameter(""));
            return Map.entry(entry.getKey(), realValue.getValue());
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
