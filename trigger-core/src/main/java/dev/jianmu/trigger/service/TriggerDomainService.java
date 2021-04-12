package dev.jianmu.trigger.service;

import dev.jianmu.trigger.aggregate.Trigger;
import dev.jianmu.trigger.aggregate.TriggerParameter;

import java.util.Map;
import java.util.Set;

/**
 * @class: TriggerDomainService
 * @description: 触发器领域服务
 * @author: Ethan Liu
 * @create: 2021-04-12 08:33
 **/
public class TriggerDomainService {
    public void handleParameterMap(
            Trigger trigger,
            Set<TriggerParameter> triggerParameters,
            Map<String, String> parameterMap
    ) {
        triggerParameters.forEach(triggerParameter -> {
            var parameterId = parameterMap.get(triggerParameter.getRef());
            triggerParameter.setParameterId(parameterId);
        });
        trigger.setTriggerParameters(triggerParameters);
    }
}
