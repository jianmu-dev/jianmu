package dev.jianmu.eventbridge.service;

import dev.jianmu.eventbridge.aggregate.EventParameter;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @class: EventParameterDomainService
 * @description: EventParameterDomainService
 * @author: Ethan Liu
 * @create: 2021-10-26 21:36
 **/
public class EventParameterDomainService {
    public static Map<String, String> convertParamMap(Set<EventParameter> eventParameters) {
        return eventParameters.stream()
                .map(eventParameter -> Map.entry(eventParameter.getName(), eventParameter.getParameterId()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
