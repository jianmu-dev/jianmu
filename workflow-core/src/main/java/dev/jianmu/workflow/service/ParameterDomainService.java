package dev.jianmu.workflow.service;


import dev.jianmu.workflow.aggregate.parameter.Parameter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @class ParameterDomainService
 * @description 参数领域服务
 * @author Ethan Liu
 * @create 2021-04-08 10:00
*/
public class ParameterDomainService {

    public Map<String, String> createParameterMap(Map<String, ? extends Parameter> parameterMap) {
        return parameterMap.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), entry.getValue().getId()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public void createParameterMap(Map<String, String> parameterMap, List<Parameter> parameters) {
        parameterMap.forEach((key, val) -> {
            parameters.stream()
                    .filter(parameter -> parameter.getId().equals(val))
                    .findFirst()
                    .ifPresent(parameter -> parameterMap.put(key, parameter.getStringValue()));
        });
    }

    public Map<String, Parameter> matchParameters(Map<String, String> parameterMap, List<Parameter> parameters) {
        return parameterMap.entrySet().stream()
                .map(entry -> {
                    var p = parameters.stream()
                            .filter(parameter -> parameter.getId().equals(entry.getValue()))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("未找到对应的参数"));
                    return Map.entry(entry.getKey(), p);
                }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
