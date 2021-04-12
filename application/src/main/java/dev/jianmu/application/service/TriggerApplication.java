package dev.jianmu.application.service;

import dev.jianmu.parameter.aggregate.Parameter;
import dev.jianmu.parameter.repository.ParameterRepository;
import dev.jianmu.parameter.service.ParameterDomainService;
import dev.jianmu.trigger.aggregate.Trigger;
import dev.jianmu.trigger.aggregate.TriggerParameter;
import dev.jianmu.trigger.repository.TriggerRepository;
import dev.jianmu.trigger.service.TriggerDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @class: TriggerApplication
 * @description: 触发器门面类
 * @author: Ethan Liu
 * @create: 2021-04-08 18:35
 **/
@Service
public class TriggerApplication {
    private final TriggerRepository triggerRepository;
    private final ParameterRepository parameterRepository;
    private final ParameterDomainService parameterDomainService;
    private final TriggerDomainService triggerDomainService;
    private final WorkflowInstanceApplication workflowInstanceApplication;

    @Inject
    public TriggerApplication(
            TriggerRepository triggerRepository,
            ParameterRepository parameterRepository,
            ParameterDomainService parameterDomainService,
            TriggerDomainService triggerDomainService,
            WorkflowInstanceApplication workflowInstanceApplication) {
        this.triggerRepository = triggerRepository;
        this.parameterRepository = parameterRepository;
        this.parameterDomainService = parameterDomainService;
        this.triggerDomainService = triggerDomainService;
        this.workflowInstanceApplication = workflowInstanceApplication;
    }

    @Transactional
    public void trigger(String triggerId) {
        var trigger = this.triggerRepository.findById(triggerId)
                .orElseThrow(() -> new RuntimeException("未找到该触发器"));
        this.workflowInstanceApplication.createAndStart(trigger.getId(), trigger.getWorkflowId());
    }

    @Transactional
    public void addWorkflowTrigger(String workflowId, Trigger.Type type, Set<TriggerParameter> triggerParameters) {
        // 创建参数存储
        var parameters = triggerParameters.stream()
                .map(triggerParameter ->
                        Map.entry(
                                triggerParameter.getRef(),
                                Parameter.Builder.aParameter()
                                        .type(triggerParameter.getType())
                                        .value(triggerParameter.getValue())
                                        .build()
                        )
                )
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        // 转换为参数ref与参数id Map
        var parameterMap = this.parameterDomainService.createParameterMap(parameters);
        var trigger = Trigger.Builder.aTrigger()
                .type(type)
                .category(Trigger.Category.WORKFLOW)
                .workflowId(workflowId)
                .workspace(workflowId)
                .build();
        // 处理参数Map
        this.triggerDomainService.handleParameterMap(trigger, triggerParameters, parameterMap);
        this.parameterRepository.addAll(new ArrayList<>(parameters.values()));
        this.triggerRepository.add(trigger);
    }

    @Transactional
    public void addTaskTrigger(String taskDefinitionId, Trigger.Type type, Set<TriggerParameter> triggerParameters) {
        // 创建参数存储
        var parameters = triggerParameters.stream()
                .map(triggerParameter ->
                        Map.entry(
                                triggerParameter.getRef(),
                                Parameter.Builder.aParameter()
                                        .type(triggerParameter.getType())
                                        .value(triggerParameter.getValue())
                                        .build()
                        )
                )
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        // 转换为参数ref与参数id Map
        var parameterMap = this.parameterDomainService.createParameterMap(parameters);
        var trigger = Trigger.Builder.aTrigger()
                .type(type)
                .category(Trigger.Category.TASK)
                .taskDefinitionId(taskDefinitionId)
                .workspace(taskDefinitionId)
                .build();
        // 处理参数Map
        this.triggerDomainService.handleParameterMap(trigger, triggerParameters, parameterMap);
        this.parameterRepository.addAll(new ArrayList<>(parameters.values()));
        this.triggerRepository.add(trigger);
    }

    @Transactional
    public void delete(String triggerId) {
        var trigger = this.triggerRepository.findById(triggerId)
                .orElseThrow(() -> new RuntimeException("该触发器不存在"));
        this.triggerRepository.delete(trigger);
    }
}
