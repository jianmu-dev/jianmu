package dev.jianmu.application.service.internal;

import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.el.ElContext;
import dev.jianmu.infrastructure.exception.DBException;
import dev.jianmu.task.repository.InstanceParameterRepository;
import dev.jianmu.task.repository.TaskInstanceRepository;
import dev.jianmu.trigger.event.TriggerEvent;
import dev.jianmu.trigger.repository.TriggerEventRepository;
import dev.jianmu.workflow.aggregate.definition.Node;
import dev.jianmu.workflow.aggregate.definition.Workflow;
import dev.jianmu.workflow.aggregate.parameter.Parameter;
import dev.jianmu.workflow.aggregate.process.ProcessStatus;
import dev.jianmu.workflow.aggregate.process.TaskStatus;
import dev.jianmu.workflow.aggregate.process.WorkflowInstance;
import dev.jianmu.workflow.el.EvaluationContext;
import dev.jianmu.workflow.el.ExpressionLanguage;
import dev.jianmu.workflow.repository.ParameterRepository;
import dev.jianmu.workflow.repository.WorkflowInstanceRepository;
import dev.jianmu.workflow.repository.WorkflowRepository;
import dev.jianmu.workflow.service.ParameterDomainService;
import dev.jianmu.workflow.service.WorkflowInstanceDomainService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @class: WorkflowInstanceInternalApplication
 * @description: WorkflowInstanceInternalApplication
 * @author: Ethan Liu
 * @create: 2021-10-21 15:35
 **/
@Service
@Slf4j
public class WorkflowInstanceInternalApplication {
    private final WorkflowRepository workflowRepository;
    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final WorkflowInstanceDomainService workflowInstanceDomainService;
    private final TaskInstanceRepository taskInstanceRepository;
    private final ExpressionLanguage expressionLanguage;
    private final InstanceParameterRepository instanceParameterRepository;
    private final ParameterDomainService parameterDomainService;
    private final TriggerEventRepository triggerEventRepository;
    private final ParameterRepository parameterRepository;

    public WorkflowInstanceInternalApplication(
            WorkflowRepository workflowRepository,
            WorkflowInstanceRepository workflowInstanceRepository,
            WorkflowInstanceDomainService workflowInstanceDomainService,
            TaskInstanceRepository taskInstanceRepository,
            ExpressionLanguage expressionLanguage,
            InstanceParameterRepository instanceParameterRepository,
            ParameterDomainService parameterDomainService,
            TriggerEventRepository triggerEventRepository,
            ParameterRepository parameterRepository
    ) {
        this.workflowRepository = workflowRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.workflowInstanceDomainService = workflowInstanceDomainService;
        this.taskInstanceRepository = taskInstanceRepository;
        this.expressionLanguage = expressionLanguage;
        this.instanceParameterRepository = instanceParameterRepository;
        this.parameterDomainService = parameterDomainService;
        this.triggerEventRepository = triggerEventRepository;
        this.parameterRepository = parameterRepository;
    }

    private EvaluationContext findContext(Workflow workflow, String instanceId, String triggerId) {
        // 查询参数源
        var eventParameters = this.triggerEventRepository.findById(triggerId)
                .map(TriggerEvent::getParameters)
                .orElseGet(List::of);
        var instanceParameters = this.instanceParameterRepository
                .findOutputParamByBusinessIdAndTriggerId(instanceId, triggerId);
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

    // 创建并启动流程
    @Transactional
    public WorkflowInstance createAndStart(String triggerId, String triggerType, String workflowRefVersion) {
        Workflow workflow = this.workflowRepository
                .findByRefVersion(workflowRefVersion)
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义"));
        // 检查是否存在运行中的流程
        int i = this.workflowInstanceRepository
                .findByRefAndVersionAndStatus(workflow.getRef(), workflow.getVersion(), ProcessStatus.RUNNING)
                .size();
        if (i > 0) {
            throw new RuntimeException("该流程运行中");
        }
        // 查询serialNo
        AtomicInteger serialNo = new AtomicInteger(1);
        this.workflowInstanceRepository.findByRefAndSerialNoMax(workflow.getRef())
                .ifPresent(workflowInstance -> serialNo.set(workflowInstance.getSerialNo() + 1));
        // 创建新的流程实例
        WorkflowInstance workflowInstance = workflowInstanceDomainService.create(triggerId, triggerType, serialNo.get(), workflow);
        workflowInstance.setExpressionLanguage(this.expressionLanguage);
        // 启动流程
        Node start = workflow.findStart();
        workflowInstanceDomainService.activateNode(workflow, workflowInstance, start.getRef());

        return this.workflowInstanceRepository.add(workflowInstance);
    }

    // 启动流程
    @Transactional
    public WorkflowInstance start(String instanceId, String nodeRef) {
        WorkflowInstance instance = this.workflowInstanceRepository
                .findById(instanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        Workflow workflow = this.workflowRepository
                .findByRefAndVersion(instance.getWorkflowRef(), instance.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义: " + instance.getWorkflowRef() + instance.getWorkflowVersion()));
        EvaluationContext context = this.findContext(workflow, instanceId, instance.getTriggerId());
        instance.setExpressionLanguage(this.expressionLanguage);
        instance.setContext(context);
        // 启动流程
        Node start = workflow.findNode(nodeRef);
        workflowInstanceDomainService.activateNode(workflow, instance, start.getRef());
        return this.workflowInstanceRepository.save(instance);
    }

    // 终止流程
    @Transactional
    public void stop(String instanceId) {
        var workflowInstance = this.workflowInstanceRepository
                .findById(instanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        // 终止流程
        MDC.put("triggerId", workflowInstance.getTriggerId());
        workflowInstance.terminate();
        // 同时终止运行中的任务
        Workflow workflow = this.workflowRepository
                .findByRefAndVersion(workflowInstance.getWorkflowRef(), workflowInstance.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义"));
        workflowInstance.getAsyncTaskInstances().stream()
                .filter(asyncTaskInstance -> asyncTaskInstance.getStatus() == TaskStatus.RUNNING)
                .forEach(asyncTaskInstance -> {
                    log.info("terminateNode: " + asyncTaskInstance.getAsyncTaskRef());
                    workflowInstanceDomainService.terminateNode(workflow, workflowInstance, asyncTaskInstance.getAsyncTaskRef());
                });
        this.workflowInstanceRepository.save(workflowInstance);
    }

    // 节点启动，重做
    @Transactional
    @Retryable(value = DBException.OptimisticLocking.class, maxAttempts = 5, backoff = @Backoff(delay = 3000L, multiplier = 2))
    public WorkflowInstance activateNode(String instanceId, String nodeRef) {
        WorkflowInstance instance = this.workflowInstanceRepository
                .findById(instanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        Workflow workflow = this.workflowRepository
                .findByRefAndVersion(instance.getWorkflowRef(), instance.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义"));
        EvaluationContext context = this.findContext(workflow, instanceId, instance.getTriggerId());
        instance.setExpressionLanguage(this.expressionLanguage);
        instance.setContext(context);
        // 激活节点
        log.info("activateNode: " + nodeRef);
        workflowInstanceDomainService.activateNode(workflow, instance, nodeRef);
        return this.workflowInstanceRepository.save(instance);
    }

    @Recover
    public WorkflowInstance recoverActivateNode(DBException.OptimisticLocking e, String instanceId, String nodeRef) {
        log.info("WorkflowInstance id {} 的节点 {} 无法激活", instanceId, nodeRef);
        log.error("------------超过重试次数-------------", e);
        return null;
    }

    // 节点跳过
    @Transactional
    @Retryable(value = DBException.OptimisticLocking.class, maxAttempts = 5, backoff = @Backoff(delay = 3000L, multiplier = 2))
    public void skipNode(String instanceId, String nodeRef) {
        WorkflowInstance instance = this.workflowInstanceRepository
                .findById(instanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        Workflow workflow = this.workflowRepository
                .findByRefAndVersion(instance.getWorkflowRef(), instance.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义"));
        // 跳过节点
        log.info("skipNode: " + nodeRef);
        workflowInstanceDomainService.skipNode(workflow, instance, nodeRef);
        this.workflowInstanceRepository.save(instance);
    }

    // 任务中止，完成
    @Transactional
    public WorkflowInstance terminateNode(String instanceId) {
        WorkflowInstance instance = this.workflowInstanceRepository
                .findById(instanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        Workflow workflow = this.workflowRepository
                .findByRefAndVersion(instance.getWorkflowRef(), instance.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义"));
        instance.getAsyncTaskInstances().stream()
                .filter(asyncTaskInstance -> asyncTaskInstance.getStatus() == TaskStatus.RUNNING)
                .forEach(asyncTaskInstance -> {
                    log.info("terminateNode: " + asyncTaskInstance.getAsyncTaskRef());
                    workflowInstanceDomainService.terminateNode(workflow, instance, asyncTaskInstance.getAsyncTaskRef());
                });
        // 中止节点
        return this.workflowInstanceRepository.save(instance);
    }

    // 任务已启动命令
    @Transactional
    @Retryable(value = DBException.OptimisticLocking.class, maxAttempts = 5, backoff = @Backoff(delay = 3000L, multiplier = 2))
    public void taskRun(String taskInstanceId) {
        // TODO 这里可以通过优化传入参数减少查询
        var taskInstance = this.taskInstanceRepository.findById(taskInstanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该任务实例"));
        var workflowInstance = this.workflowInstanceRepository
                .findById(taskInstance.getBusinessId())
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        workflowInstance.taskRun(taskInstance.getAsyncTaskRef(), taskInstanceId);
        this.workflowInstanceRepository.save(workflowInstance);
    }

    @Recover
    public void recoverTask(DBException.OptimisticLocking e, String taskInstanceId) {
        log.info("taskInstance id {} 的任务无法修改状态", taskInstanceId);
        log.error("------------超过重试次数-------------", e);
    }

    // 任务已中止命令
    @Transactional
    @Retryable(value = DBException.OptimisticLocking.class, maxAttempts = 5, backoff = @Backoff(delay = 3000L, multiplier = 2))
    public void taskFail(String taskInstanceId) {
        // TODO 这里可以通过优化传入参数减少查询
        var taskInstance = this.taskInstanceRepository.findById(taskInstanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该任务实例"));
        var workflowInstance = this.workflowInstanceRepository
                .findById(taskInstance.getBusinessId())
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        workflowInstance.taskFail(taskInstance.getAsyncTaskRef());
        this.workflowInstanceRepository.save(workflowInstance);
    }

    // 任务已成功命令
    @Transactional
    @Retryable(value = DBException.OptimisticLocking.class, maxAttempts = 5, backoff = @Backoff(delay = 3000L, multiplier = 2))
    public void taskSucceed(String taskInstanceId) {
        // TODO 这里可以通过优化传入参数减少查询
        var taskInstance = this.taskInstanceRepository.findById(taskInstanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该任务实例"));
        var workflowInstance = this.workflowInstanceRepository
                .findById(taskInstance.getBusinessId())
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        Workflow workflow = this.workflowRepository
                .findByRefAndVersion(workflowInstance.getWorkflowRef(), workflowInstance.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义"));
        // 任务执行成功
        log.info("taskSucceed: " + taskInstance.getAsyncTaskRef());
        this.workflowInstanceDomainService.taskSucceed(workflow, workflowInstance, taskInstance.getAsyncTaskRef());
        this.workflowInstanceRepository.save(workflowInstance);
    }
}
