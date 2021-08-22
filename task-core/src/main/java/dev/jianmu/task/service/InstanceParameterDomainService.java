package dev.jianmu.task.service;

import dev.jianmu.task.aggregate.InstanceParameter;
import dev.jianmu.task.aggregate.TaskInstance;
import dev.jianmu.task.aggregate.TaskParameter;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @class: InstanceParameterDomainService
 * @description: InstanceParameterDomainService
 * @author: Ethan Liu
 * @create: 2021-04-29 13:17
 **/
public class InstanceParameterDomainService {
    public static Set<InstanceParameter> createInstanceParameters(Set<TaskParameter> taskParameters, TaskInstance taskInstance) {
        return taskParameters.stream()
                .map(taskParameter -> InstanceParameter.Builder.anInstanceParameter()
                        .instanceId(taskInstance.getId())
                        .triggerId(taskInstance.getTriggerId())
                        .defKey(taskInstance.getDefKey())
                        .asyncTaskRef(taskInstance.getAsyncTaskRef())
                        .businessId(taskInstance.getBusinessId())
                        .ref(taskParameter.getRef())
                        .parameterId(taskParameter.getParameterId())
                        .type(InstanceParameter.Type.INPUT)
                        .build()
                )
                .collect(Collectors.toSet());
    }
}
