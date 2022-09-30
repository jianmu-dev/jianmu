package dev.jianmu.application.service.internal;

import dev.jianmu.application.command.WorkflowStartCmd;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.infrastructure.GlobalProperties;
import dev.jianmu.project.repository.ProjectLastExecutionRepository;
import dev.jianmu.project.repository.ProjectRepository;
import dev.jianmu.trigger.event.TriggerFailedEvent;
import dev.jianmu.workflow.aggregate.definition.Workflow;
import dev.jianmu.workflow.aggregate.process.ProcessStatus;
import dev.jianmu.workflow.aggregate.process.WorkflowInstance;
import dev.jianmu.workflow.repository.AsyncTaskInstanceRepository;
import dev.jianmu.workflow.repository.WorkflowInstanceRepository;
import dev.jianmu.workflow.repository.WorkflowRepository;
import dev.jianmu.workflow.service.WorkflowInstanceDomainService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

    public WorkflowInstanceInternalApplication(
            WorkflowRepository workflowRepository,
            WorkflowInstanceRepository workflowInstanceRepository,
            AsyncTaskInstanceRepository asyncTaskInstanceRepository,
            WorkflowInstanceDomainService workflowInstanceDomainService,
            ApplicationEventPublisher publisher,
            ProjectRepository projectRepository,
            GlobalProperties globalProperties,
            ProjectLastExecutionRepository projectLastExecutionRepository
    ) {
        this.workflowRepository = workflowRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.asyncTaskInstanceRepository = asyncTaskInstanceRepository;
        this.workflowInstanceDomainService = workflowInstanceDomainService;
        this.publisher = publisher;
        this.projectRepository = projectRepository;
        this.globalProperties = globalProperties;
        this.projectLastExecutionRepository = projectLastExecutionRepository;
    }

    // 创建流程
    @Transactional
    public void create(WorkflowStartCmd cmd, String projectId) {
        Workflow workflow = this.workflowRepository
                .findByRefAndVersion(cmd.getWorkflowRef(), cmd.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义"));
        var project = this.projectRepository.findById(projectId)
                .orElseThrow(() -> new DataNotFoundException("未找到项目ID:" + projectId));
        var projectLastExecution = this.projectLastExecutionRepository.findByRef(project.getWorkflowRef())
                .orElseThrow(() -> new DataNotFoundException("未找到项目最后执行记录"));
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
        projectLastExecution.init(workflowInstance.getId(), workflowInstance.getSerialNo(), workflowInstance.getStatus().name());
        this.workflowInstanceRepository.add(workflowInstance);
        this.projectLastExecutionRepository.update(projectLastExecution);

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
                    .forEach(workflowInstance -> {
                        workflowInstance.createVolume();
                        // 修改项目最后执行状态
                        projectLastExecution.running(workflowInstance.getId(), workflowInstance.getSerialNo(), workflowInstance.getStartTime(), workflowInstance.getStatus().name());
                        this.workflowInstanceRepository.save(workflowInstance);
                        this.projectLastExecutionRepository.update(projectLastExecution);
                    });
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
                .ifPresent(workflowInstance -> {
                    workflowInstance.createVolume();
                    // 修改项目最后执行状态
                    projectLastExecution.running(workflowInstance.getId(), workflowInstance.getSerialNo(), workflowInstance.getStartTime(), workflowInstance.getStatus().name());
                    this.workflowInstanceRepository.save(workflowInstance);
                    this.projectLastExecutionRepository.update(projectLastExecution);
                });
    }

    // 流程正常结束
    @Transactional
    public void end(String triggerId) {
        var workflowInstance = this.workflowInstanceRepository.findByTriggerId(triggerId)
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        var projectLastExecution = this.projectLastExecutionRepository.findByRef(workflowInstance.getWorkflowRef())
                .orElseThrow(() -> new DataNotFoundException("未找到项目最后执行记录"));
        workflowInstance.end();
        projectLastExecution.end(workflowInstance.getId(), workflowInstance.getSerialNo(), workflowInstance.getStatus().name(), workflowInstance.getStartTime(), workflowInstance.getEndTime());
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
        projectLastExecution.suspend(workflowInstance.getId(), workflowInstance.getSerialNo(), workflowInstance.getStatus().name(), workflowInstance.getSuspendedTime());
        this.workflowInstanceRepository.save(workflowInstance);
        this.projectLastExecutionRepository.update(projectLastExecution);
    }

    @Transactional
    public void resume(String instanceId, String taskRef) {
        var workflowInstance = this.workflowInstanceRepository.findById(instanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        var projectLastExecution = this.projectLastExecutionRepository.findByRef(workflowInstance.getWorkflowRef())
                .orElseThrow(() -> new DataNotFoundException("未找到项目最后执行记录"));
        // 恢复流程
        MDC.put("triggerId", workflowInstance.getTriggerId());
        var asyncTaskInstances = this.asyncTaskInstanceRepository.findByInstanceId(instanceId);
        if (this.workflowInstanceDomainService.canResume(asyncTaskInstances, taskRef)) {
            workflowInstance.resume();
            projectLastExecution.resume(workflowInstance.getId(), workflowInstance.getSerialNo(), workflowInstance.getStartTime(), workflowInstance.getStatus().name());
            this.workflowInstanceRepository.save(workflowInstance);
            this.projectLastExecutionRepository.update(projectLastExecution);
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
        projectLastExecution.end(workflowInstance.getId(), workflowInstance.getSerialNo(), workflowInstance.getStatus().name(), workflowInstance.getStartTime(), workflowInstance.getEndTime());
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
        projectLastExecution.end(workflowInstance.getId(), workflowInstance.getSerialNo(), workflowInstance.getStatus().name(), workflowInstance.getStartTime(), workflowInstance.getEndTime());
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
}
