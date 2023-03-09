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
import dev.jianmu.workflow.event.definition.WorkflowErrorEvent;
import dev.jianmu.workflow.repository.AsyncTaskInstanceRepository;
import dev.jianmu.workflow.repository.ParameterRepository;
import dev.jianmu.workflow.repository.WorkflowInstanceRepository;
import dev.jianmu.workflow.repository.WorkflowRepository;
import dev.jianmu.workflow.service.ParameterDomainService;
import dev.jianmu.workflow.service.WorkflowDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
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
    @Resource
    private ApplicationEventPublisher publisher;

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
    public void init(WorkflowStartCmd cmd) {
        var workflowInstance = this.workflowInstanceRepository.findByTriggerId(cmd.getTriggerId())
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        if (!workflowInstance.isRunning()) {
            log.warn("该流程实例已结束，无法创建新任务");
            return;
        }
        Workflow workflow = this.workflowRepository
                .findByRefAndVersion(cmd.getWorkflowRef(), cmd.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义"));
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
                        .failureMode(node.getFailureMode())
                        .build()
        ).collect(Collectors.toList());

        try {
            this.asyncTaskInstanceRepository.addAll(asyncTaskInstances);
        } catch (DuplicateKeyException e) {
            log.info("AsyncTaskInstance唯一索引重复");
            return;
        }
        this.workflowRepository.commitEvents(workflow);
    }

    // TODO 待删除
    @Transactional
    public void start(WorkflowStartCmd cmd) {
        Workflow workflow = this.workflowRepository
                .findByRefAndVersion(cmd.getWorkflowRef(), cmd.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义"));
        // 启动流程
        workflow.start(cmd.getTriggerId());
        this.workflowRepository.commitEvents(workflow);
    }

    @Transactional
    public void next(NextNodeCmd cmd) {
        this.publisher.publishEvent(
                WorkflowErrorEvent.Builder.aWorkflowErrorEvent()
                        .workflowRef(cmd.getWorkflowRef())
                        .workflowVersion(cmd.getWorkflowVersion())
                        .triggerId(cmd.getTriggerId())
                        .nodeRef(cmd.getNodeRef())
                        .build()
        );
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
        this.publisher.publishEvent(
                WorkflowErrorEvent.Builder.aWorkflowErrorEvent()
                        .workflowRef(cmd.getWorkflowRef())
                        .workflowVersion(cmd.getWorkflowVersion())
                        .triggerId(cmd.getTriggerId())
                        .nodeRef(cmd.getNodeRef())
                        .sender(cmd.getSender())
                        .build()
        );
        Workflow workflow = this.workflowRepository
                .findByRefAndVersion(cmd.getWorkflowRef(), cmd.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义"));
        // 激活节点
        var asyncTaskInstances = this.asyncTaskInstanceRepository.findByTriggerId(cmd.getTriggerId());
        if (this.workflowDomainService.canActivateNode(cmd.getNodeRef(), cmd.getSender(), workflow, asyncTaskInstances)) {
            asyncTaskInstances.stream()
                    .filter(t -> t.getAsyncTaskRef().equals(cmd.getNodeRef()))
                    .forEach(t -> this.doActivate(workflow, cmd.getNodeRef(), cmd.getTriggerId(), t.getVersion()));
        }
    }

    private void doActivate(Workflow workflow, String nodeRef, String triggerId, int version) {
        log.info("activateNode: " + nodeRef);
        EvaluationContext context = this.findContext(workflow, triggerId);
        workflow.setExpressionLanguage(this.expressionLanguage);
        workflow.setContext(context);
        workflow.activateNode(triggerId, nodeRef, version);
        this.workflowRepository.commitEvents(workflow);
    }

    private void doSkip(Workflow workflow, String nodeRef, String triggerId) {
        workflow.skipNode(triggerId, nodeRef);
        log.info("跳过节点: {}", nodeRef);
        this.asyncTaskInstanceRepository
                .findByTriggerIdAndTaskRef(triggerId, nodeRef)
                .ifPresent(asyncTaskInstance -> {
                    asyncTaskInstance.skip();
                    log.info("跳过异步任务: {}", asyncTaskInstance.getAsyncTaskRef());
                    this.asyncTaskInstanceRepository.updateById(asyncTaskInstance);
                });
        this.workflowRepository.commitEvents(workflow);
    }

    // 节点跳过
    @Transactional
    public void skipNode(SkipNodeCmd cmd) {
        this.publisher.publishEvent(
                WorkflowErrorEvent.Builder.aWorkflowErrorEvent()
                        .workflowRef(cmd.getWorkflowRef())
                        .workflowVersion(cmd.getWorkflowVersion())
                        .triggerId(cmd.getTriggerId())
                        .nodeRef(cmd.getNodeRef())
                        .sender(cmd.getSender())
                        .build()
        );
        Workflow workflow = this.workflowRepository
                .findByRefAndVersion(cmd.getWorkflowRef(), cmd.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义"));
        var asyncTaskInstances = this.asyncTaskInstanceRepository.findByTriggerId(cmd.getTriggerId());
        if (this.workflowDomainService.canSkipNode(cmd.getNodeRef(), cmd.getSender(), workflow, asyncTaskInstances)) {
            this.doSkip(workflow, cmd.getNodeRef(), cmd.getTriggerId());
            return;
        }
        log.info("不能跳过，计算是否需要激活节点");
        if (!this.workflowDomainService.hasSameSerialNo(cmd.getNodeRef(), workflow, asyncTaskInstances)) {
            log.info("找到不同次数的节点，无需计算激活");
            return;
        }
        if (this.workflowDomainService.canActivateNode(cmd.getNodeRef(), cmd.getSender(), workflow, asyncTaskInstances)) {
            asyncTaskInstances.stream()
                    .filter(t -> t.getAsyncTaskRef().equals(cmd.getNodeRef()))
                    .forEach(t -> this.doActivate(workflow, cmd.getNodeRef(), cmd.getTriggerId(), t.getVersion()));
        }
    }

    public List<Workflow> findByRefVersions(List<String> refVersions) {
        return this.workflowRepository.findByRefVersions(refVersions);
    }

    public Optional<Workflow> findByRefAndVersion(String workflowRef, String workflowVersion) {
        return this.workflowRepository.findByRefAndVersion(workflowRef, workflowVersion);
    }
}
