package dev.jianmu.task.service;

import dev.jianmu.task.aggregate.InstanceParameter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @class: InstanceParameterDomainService
 * @description: InstanceParameterDomainService
 * @author: Ethan Liu
 * @create: 2021-10-26 21:41
 **/
public class InstanceParameterDomainService {
    public static Map<String, String> convertParamMap(List<InstanceParameter> instanceParameters) {
        return instanceParameters.stream()
                // 输出参数scope为asyncTaskRef
                .map(instanceParameter -> Map.entry(instanceParameter.getAsyncTaskRef() + "." + instanceParameter.getRef(), instanceParameter.getParameterId()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
