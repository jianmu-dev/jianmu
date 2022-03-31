package dev.jianmu.application.service.internal;

import dev.jianmu.application.command.ActivateNodeCmd;
import dev.jianmu.application.command.NextNodeCmd;
import dev.jianmu.application.command.SkipNodeCmd;
import dev.jianmu.application.command.WorkflowStartCmd;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.el.ElContext;
import dev.jianmu.task.repository.InstanceParameterRepository;
import dev.jianmu.trigger.event.TriggerEvent;
import dev.jianmu.trigger.repository.TriggerEventRepository;
import dev.jianmu.workflow.aggregate.definition.Workflow;
import dev.jianmu.workflow.aggregate.parameter.Parameter;
import dev.jianmu.workflow.aggregate.process.AsyncTaskInstance;
import dev.jianmu.workflow.el.EvaluationContext;
import dev.jianmu.workflow.el.ExpressionLanguage;
import dev.jianmu.workflow.repository.AsyncTaskInstanceRepository;
import dev.jianmu.workflow.repository.ParameterRepository;
import dev.jianmu.workflow.repository.WorkflowInstanceRepository;
import dev.jianmu.workflow.repository.WorkflowRepository;
import dev.jianmu.workflow.service.ParameterDomainService;
import dev.jianmu.workflow.service.WorkflowDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Ethan Liu
 * @class WorkflowInternalApplication
 * @description WorkflowInternalApplication
 * @create 2022-01-01 10:53
 */
@Service
@Slf4j
public class WorkflowInternalApplication {
    private final WorkflowRepository workflowRepository;
    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final AsyncTaskInstanceRepository asyncTaskInstanceRepository;
    private final ExpressionLanguage expressionLanguage;
    private final InstanceParameterRepository instanceParameterRepository;
    private final ParameterDomainService parameterDomainService;
    private final TriggerEventRepository triggerEventRepository;
    private final ParameterRepository parameterRepository;
    private final WorkflowDomainService workflowDomainService = new WorkflowDomainService();

    public WorkflowInternalApplication(
            WorkflowRepository workflowRepository,
            WorkflowInstanceRepository workflowInstanceRepository,
            AsyncTaskInstanceRepository asyncTaskInstanceRepository,
            ExpressionLanguage expressionLanguage,
            InstanceParameterRepository instanceParameterRepository,
            ParameterDomainService parameterDomainService,
            TriggerEventRepository triggerEventRepository,
            ParameterRepository parameterRepository
    ) {
        this.workflowRepository = workflowRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.asyncTaskInstanceRepository = asyncTaskInstanceRepository;
        this.expressionLanguage = expressionLanguage;
        this.instanceParameterRepository = instanceParameterRepository;
        this.parameterDomainService = parameterDomainService;
        this.triggerEventRepository = triggerEventRepository;
        this.parameterRepository = parameterRepository;
    }

    private EvaluationContext findContext(Workflow workflow, String triggerId) {
        // 查询参数源
        var eventParameters = this.triggerEventRepository.findById(triggerId)
                .map(TriggerEvent::getParameters)
                .orElseGet(List::of);
        var instanceParameters = this.instanceParameterRepository
                .findLastOutputParamByTriggerId(triggerId);
        // 创建表达式上下文
        var context = new ElContext();
        // 全局参数加入上下文
        workflow.getGlobalParameters()
                .forEach(globalParameter -> context.add(
                        "global",
                        globalParameter.getName(),
                        Parameter.Type.getTypeByName(globalParameter.getType()).newParameter(globalParameter.getValue()))
                );
        // 事件参数加入上下文
        var eventParams = eventParameters.stream()
                .map(eventParameter -> Map.entry(eventParameter.getName(), eventParameter.getParameterId()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        var eventParamValues = this.parameterRepository.findByIds(new HashSet<>(eventParams.values()));
        var eventMap = this.parameterDomainService.matchParameters(eventParams, eventParamValues);
        // 事件参数scope为event
        eventMap.forEach((key, val) -> context.add("trigger", key, val));
        // 任务输出参数加入上下文
        Map<String, String> outParams = new HashMap<>();
        instanceParameters.forEach(instanceParameter -> {
            // 输出参数scope为workflowType.asyncTaskRef
            outParams.put(instanceParameter.getWorkflowType() + "." + instanceParameter.getAsyncTaskRef() + "." + instanceParameter.getRef(), instanceParameter.getParameterId());
            // 输出参数scope为asyncTaskRef
            outParams.put(instanceParameter.getAsyncTaskRef() + "." + instanceParameter.getRef(), instanceParameter.getParameterId());
        });
        var outParamValues = this.parameterRepository.findByIds(new HashSet<>(outParams.values()));
        var outMap = this.parameterDomainService.matchParameters(outParams, outParamValues);
        outMap.forEach(context::add);
        return context;
    }

    @Transactional
    public void start(WorkflowStartCmd cmd) {
        var workflowInstance = this.workflowInstanceRepository.findByTriggerId(cmd.getTriggerId())
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        if (!workflowInstance.isRunning()) {
            log.warn("该流程实例已结束，无法创建新任务");
            return;
        }
        Workflow workflow = this.workflowRepository
                .findByRefAndVersion(cmd.getWorkflowRef(), cmd.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义"));
        // 启动流程
        workflow.start(cmd.getTriggerId());
        var asyncTaskInstances = workflow.getNodes().stream().map(node ->
                AsyncTaskInstance.Builder
                        .anAsyncTaskInstance()
                        .workflowInstanceId(workflowInstance.getId())
                        .triggerId(cmd.getTriggerId())
                        .workflowRef(cmd.getWorkflowRef())
                        .workflowVersion(cmd.getWorkflowVersion())
                        .name(node.getName())
                        .description(node.getDescription())
                        .asyncTaskRef(node.getRef())
                        .asyncTaskType(node.getType())
                        .build()
        ).collect(Collectors.toList());

        this.asyncTaskInstanceRepository.addAll(asyncTaskInstances);
        this.workflowRepository.commitEvents(workflow);
    }

    @Transactional
    public void next(NextNodeCmd cmd) {
        Workflow workflow = this.workflowRepository
                .findByRefAndVersion(cmd.getWorkflowRef(), cmd.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义"));
        EvaluationContext context = this.findContext(workflow, cmd.getTriggerId());
        workflow.setExpressionLanguage(this.expressionLanguage);
        workflow.setContext(context);
        workflow.next(cmd.getTriggerId(), cmd.getNodeRef());
        this.workflowRepository.commitEvents(workflow);
    }

    // 节点启动
    @Transactional
    public void activateNode(ActivateNodeCmd cmd) {
        Workflow workflow = this.workflowRepository
                .findByRefAndVersion(cmd.getWorkflowRef(), cmd.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义"));
        // 激活节点
        var asyncTaskInstances = this.asyncTaskInstanceRepository.findByTriggerId(cmd.getTriggerId());
        if (this.workflowDomainService.canActivateNode(cmd.getNodeRef(), cmd.getSender(), workflow, asyncTaskInstances)) {
            log.info("activateNode: " + cmd.getNodeRef());
            EvaluationContext context = this.findContext(workflow, cmd.getTriggerId());
            workflow.setExpressionLanguage(this.expressionLanguage);
            workflow.setContext(context);
            workflow.activateNode(cmd.getTriggerId(), cmd.getNodeRef());
            this.workflowRepository.commitEvents(workflow);
        }
    }

    // 节点跳过
    @Transactional
    public void skipNode(SkipNodeCmd cmd) {
        Workflow workflow = this.workflowRepository
                .findByRefAndVersion(cmd.getWorkflowRef(), cmd.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义"));
        var asyncTaskInstances = this.asyncTaskInstanceRepository.findByTriggerId(cmd.getTriggerId());
        if (this.workflowDomainService.canSkipNode(cmd.getNodeRef(), cmd.getSender(), workflow, asyncTaskInstances)) {
            workflow.skipNode(cmd.getTriggerId(), cmd.getNodeRef());
            log.info("跳过节点: {}", cmd.getNodeRef());
            this.asyncTaskInstanceRepository
                    .findByTriggerIdAndTaskRef(cmd.getTriggerId(), cmd.getNodeRef())
                    .ifPresent(asyncTaskInstance -> {
                        asyncTaskInstance.skip();
                        log.info("跳过异步任务: {}", asyncTaskInstance.getAsyncTaskRef());
                        this.asyncTaskInstanceRepository.updateById(asyncTaskInstance);
                    });
            this.workflowRepository.commitEvents(workflow);
        } else {
            log.info("不能跳过，计算是否需要激活节点");
            if (this.workflowDomainService.canActivateNode(cmd.getNodeRef(), cmd.getSender(), workflow, asyncTaskInstances)) {
                log.info("activateNode: " + cmd.getNodeRef());
                workflow.activateNode(cmd.getTriggerId(), cmd.getNodeRef());
                this.workflowRepository.commitEvents(workflow);
            }
        }
    }
}
