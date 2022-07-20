package dev.jianmu.application.service.internal;

import dev.jianmu.application.command.WorkflowStartCmd;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.el.ElContext;
import dev.jianmu.infrastructure.GlobalProperties;
import dev.jianmu.project.aggregate.ProjectLastExecution;
import dev.jianmu.project.repository.ProjectLastExecutionRepository;
import dev.jianmu.project.repository.ProjectRepository;
import dev.jianmu.trigger.event.TriggerEvent;
import dev.jianmu.trigger.event.TriggerFailedEvent;
import dev.jianmu.trigger.repository.TriggerEventRepository;
import dev.jianmu.workflow.aggregate.definition.GlobalParameter;
import dev.jianmu.workflow.aggregate.definition.Workflow;
import dev.jianmu.workflow.aggregate.process.ProcessStatus;
import dev.jianmu.workflow.aggregate.process.WorkflowInstance;
import dev.jianmu.workflow.el.ExpressionLanguage;
import dev.jianmu.workflow.el.ResultType;
import dev.jianmu.workflow.repository.AsyncTaskInstanceRepository;
import dev.jianmu.workflow.repository.ParameterRepository;
import dev.jianmu.workflow.repository.WorkflowInstanceRepository;
import dev.jianmu.workflow.repository.WorkflowRepository;
import dev.jianmu.workflow.service.ParameterDomainService;
import dev.jianmu.workflow.service.WorkflowInstanceDomainService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Ethan Liu
 * @class WorkflowInstanceInternalApplication
 * @description WorkflowInstanceInternalApplication
 * @create 2021-10-21 15:35
 */
@Service
@Slf4j
public class WorkflowInstanceInternalApplication {
    private final WorkflowRepository workflowRepository;
    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final AsyncTaskInstanceRepository asyncTaskInstanceRepository;
    private final WorkflowInstanceDomainService workflowInstanceDomainService;
    private final ApplicationEventPublisher publisher;
    private final ProjectRepository projectRepository;
    private final GlobalProperties globalProperties;
    private final ProjectLastExecutionRepository projectLastExecutionRepository;
    private final ExpressionLanguage expressionLanguage;
    private final TriggerEventRepository triggerEventRepository;
    private final ParameterRepository parameterRepository;
    private final ParameterDomainService parameterDomainService;


    public WorkflowInstanceInternalApplication(
            WorkflowRepository workflowRepository,
            WorkflowInstanceRepository workflowInstanceRepository,
            AsyncTaskInstanceRepository asyncTaskInstanceRepository,
            WorkflowInstanceDomainService workflowInstanceDomainService,
            ApplicationEventPublisher publisher,
            ProjectRepository projectRepository,
            GlobalProperties globalProperties,
            ProjectLastExecutionRepository projectLastExecutionRepository,
            ExpressionLanguage expressionLanguage,
            TriggerEventRepository triggerEventRepository,
            ParameterRepository parameterRepository,
            ParameterDomainService parameterDomainService
    ) {
        this.workflowRepository = workflowRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.asyncTaskInstanceRepository = asyncTaskInstanceRepository;
        this.workflowInstanceDomainService = workflowInstanceDomainService;
        this.publisher = publisher;
        this.projectRepository = projectRepository;
        this.globalProperties = globalProperties;
        this.projectLastExecutionRepository = projectLastExecutionRepository;
        this.expressionLanguage = expressionLanguage;
        this.triggerEventRepository = triggerEventRepository;
        this.parameterRepository = parameterRepository;
        this.parameterDomainService = parameterDomainService;
    }

    // 创建流程
    @Transactional
    public void create(WorkflowStartCmd cmd, String projectId) {
        Workflow workflow = this.workflowRepository
                .findByRefAndVersion(cmd.getWorkflowRef(), cmd.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义"));
        var project = this.projectRepository.findById(projectId)
                .orElseThrow(() -> new DataNotFoundException("未找到项目ID:" + projectId));
        if (!project.isConcurrent()) {
            // 查询待运行的流程数
            int i = this.workflowInstanceRepository
                    .findByRefAndStatuses(workflow.getRef(), List.of(ProcessStatus.INIT))
                    .size();
            if (i >= this.globalProperties.getTriggerQueue().getMax()) {
                var triggerFailedEvent = TriggerFailedEvent.Builder.aTriggerFailedEvent()
                        .triggerId(cmd.getTriggerId())
                        .triggerType(cmd.getTriggerType())
                        .build();
                this.publisher.publishEvent(triggerFailedEvent);
                throw new RuntimeException("待执行流程数已超过最大值：" + this.globalProperties.getTriggerQueue().getMax());
            }
        }
        // 查询serialNo
        AtomicInteger serialNo = new AtomicInteger(1);
        this.workflowInstanceRepository.findByRefAndSerialNoMax(workflow.getRef())
                .ifPresent(workflowInstance -> serialNo.set(workflowInstance.getSerialNo() + 1));
        // 创建新的流程实例
        WorkflowInstance workflowInstance = workflowInstanceDomainService.create(cmd.getTriggerId(), cmd.getTriggerType(), serialNo.get(), workflow);
        workflowInstance.init();
        this.workflowInstanceRepository.add(workflowInstance);
    }

    private Set<GlobalParameter> findGlobalParameters(String triggerId, String workflowRef, String version) {
        var workflow = this.workflowRepository.findByRefAndVersion(workflowRef, version)
                .orElseThrow(() -> new DataNotFoundException("未找到流程"));
        // 查询参数源
        var eventParameters = this.triggerEventRepository.findById(triggerId)
                .map(TriggerEvent::getParameters)
                .orElseGet(List::of);
        // 创建表达式上下文
        var context = new ElContext();
        // 事件参数加入上下文
        var eventParams = eventParameters.stream()
                .map(eventParameter -> Map.entry(eventParameter.getRef(), eventParameter.getParameterId()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        var eventParamValues = this.parameterRepository.findByIds(new HashSet<>(eventParams.values()));
        var eventMap = this.parameterDomainService.matchParameters(eventParams, eventParamValues);
        // 事件参数scope为event
        eventMap.forEach((key, val) -> context.add("trigger", key, val));
        var globalParameters = new HashSet<GlobalParameter>();
        workflow.getGlobalParameters()
                .forEach(globalParameter -> {
                            var expression = expressionLanguage.parseExpression(globalParameter.getValue().toString(), ResultType.valueOf(globalParameter.getType()));
                            var result = this.expressionLanguage.evaluateExpression(expression, context);
                            if (result.isFailure()) {
                                if (globalParameter.getRequired()) {
                                    throw new RuntimeException("全局参数: " + globalParameter.getRef() + " required: true 表达式: " + expression.getExpression() + " 计算错误: " + result.getFailureMessage());
                                }
                                log.warn("全局参数: {} 表达式: {} 计算错误: {}", globalParameter.getName(), expression.getExpression(), result.getFailureMessage());
                            } else {
                                globalParameters.add(GlobalParameter.Builder.aGlobalParameter()
                                        .ref(globalParameter.getRef())
                                        .name(globalParameter.getName())
                                        .type(globalParameter.getType())
                                        .value(result.getValue().getValue())
                                        .required(globalParameter.getRequired())
                                        .build());
                            }
                        }
                );
        return globalParameters;
    }

    // 启动流程
    @Transactional
    public void start(String workflowRef) {
        var project = this.projectRepository.findByWorkflowRef(workflowRef)
                .orElseThrow(() -> new DataNotFoundException("未找到项目, ref::" + workflowRef));
        var projectLastExecution = this.projectLastExecutionRepository.findByRef(project.getWorkflowRef())
                .orElseThrow(() -> new DataNotFoundException("未找到项目最后执行记录"));
        if (project.isConcurrent()) {
            this.workflowInstanceRepository.findByRefAndStatuses(workflowRef, List.of(ProcessStatus.INIT))
                    .forEach(workflowInstance -> this.createVolume(workflowInstance, projectLastExecution));
            return;
        }
        // 检查是否存在运行中的流程
        int i = this.workflowInstanceRepository
                .findByRefAndStatuses(workflowRef, List.of(ProcessStatus.RUNNING, ProcessStatus.SUSPENDED))
                .size();
        if (i > 0) {
            return;
        }
        this.workflowInstanceRepository.findByRefAndStatusAndSerialNoMin(workflowRef, ProcessStatus.INIT)
                .ifPresent(workflowInstance -> this.createVolume(workflowInstance, projectLastExecution));
    }

    // 流程实例创建Volume
    private void createVolume(WorkflowInstance workflowInstance, ProjectLastExecution projectLastExecution) {
        MDC.put("triggerId", workflowInstance.getTriggerId());
        workflowInstance.createVolume();
        // 添加全局参数
        try {
            var globalParameters = this.findGlobalParameters(workflowInstance.getTriggerId(), workflowInstance.getWorkflowRef(), workflowInstance.getWorkflowVersion());
            workflowInstance.setGlobalParameters(globalParameters);
        } catch (Exception e) {
            log.error("流程实例启动失败：", e);
            workflowInstance.terminate();
        }
        // 修改项目最后执行状态
        projectLastExecution.running(workflowInstance.getStartTime(), workflowInstance.getStatus().name());

        this.workflowInstanceRepository.save(workflowInstance);
        this.projectLastExecutionRepository.update(projectLastExecution);
    }

    // 流程正常结束
    @Transactional
    public void end(String triggerId) {
        var workflowInstance = this.workflowInstanceRepository.findByTriggerId(triggerId)
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        var projectLastExecution = this.projectLastExecutionRepository.findByRef(workflowInstance.getWorkflowRef())
                .orElseThrow(() -> new DataNotFoundException("未找到项目最后执行记录"));
        workflowInstance.end();
        projectLastExecution.end(workflowInstance.getStatus().name(), workflowInstance.getEndTime());
        this.workflowInstanceRepository.save(workflowInstance);
        this.projectLastExecutionRepository.update(projectLastExecution);
    }

    // 停止流程
    @Async
    @Transactional
    public void suspend(String instanceId) {
        var workflowInstance = this.workflowInstanceRepository.findById(instanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        var projectLastExecution = this.projectLastExecutionRepository.findByRef(workflowInstance.getWorkflowRef())
                .orElseThrow(() -> new DataNotFoundException("未找到项目最后执行记录"));
        // 终止流程
        MDC.put("triggerId", workflowInstance.getTriggerId());
        workflowInstance.suspend();
        projectLastExecution.suspend(workflowInstance.getStatus().name(), workflowInstance.getSuspendedTime());
        this.workflowInstanceRepository.save(workflowInstance);
        this.projectLastExecutionRepository.update(projectLastExecution);
    }

    @Transactional
    public void resume(String instanceId, String taskRef) {
        var workflowInstance = this.workflowInstanceRepository.findById(instanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        // 恢复流程
        MDC.put("triggerId", workflowInstance.getTriggerId());
        var asyncTaskInstances = this.asyncTaskInstanceRepository.findByInstanceId(instanceId);
        if (this.workflowInstanceDomainService.canResume(asyncTaskInstances, taskRef)) {
            workflowInstance.resume();
            this.workflowInstanceRepository.save(workflowInstance);
        }
    }

    // 终止流程
    @Async
    @Transactional
    public void terminate(String instanceId) {
        var workflowInstance = this.workflowInstanceRepository.findById(instanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        var projectLastExecution = this.projectLastExecutionRepository.findByRef(workflowInstance.getWorkflowRef())
                .orElseThrow(() -> new DataNotFoundException("未找到项目最后执行记录"));
        // 终止流程
        MDC.put("triggerId", workflowInstance.getTriggerId());
        workflowInstance.terminate();
        projectLastExecution.end(workflowInstance.getStatus().name(), workflowInstance.getEndTime());
        this.workflowInstanceRepository.save(workflowInstance);
        this.projectLastExecutionRepository.update(projectLastExecution);
    }

    // 终止流程
    @Async
    @Transactional
    public void terminateByTriggerId(String triggerId) {
        var workflowInstance = this.workflowInstanceRepository.findByTriggerId(triggerId)
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        var projectLastExecution = this.projectLastExecutionRepository.findByRef(workflowInstance.getWorkflowRef())
                .orElseThrow(() -> new DataNotFoundException("未找到项目最后执行记录"));
        // 终止流程
        MDC.put("triggerId", workflowInstance.getTriggerId());
        workflowInstance.terminate();
        projectLastExecution.end(workflowInstance.getStatus().name(), workflowInstance.getEndTime());
        this.workflowInstanceRepository.save(workflowInstance);
        this.projectLastExecutionRepository.update(projectLastExecution);
    }

    @Transactional
    public void statusCheck(String triggerId) {
        var workflowInstance = this.workflowInstanceRepository.findByTriggerId(triggerId)
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        workflowInstance.statusCheck();
        this.workflowInstanceRepository.commitEvents(workflowInstance);
    }

    public Optional<WorkflowInstance> findById(String instanceId) {
        return this.workflowInstanceRepository.findById(instanceId);
    }
}
