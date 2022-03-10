package dev.jianmu.api.eventhandler;

import dev.jianmu.application.command.WorkflowStartCmd;
import dev.jianmu.application.service.internal.AsyncTaskInstanceInternalApplication;
import dev.jianmu.application.service.internal.WorkerApplication;
import dev.jianmu.application.service.internal.WorkflowInternalApplication;
import dev.jianmu.workflow.aggregate.process.WorkflowInstance;
import dev.jianmu.workflow.event.process.ProcessEndedEvent;
import dev.jianmu.workflow.event.process.ProcessNotRunningEvent;
import dev.jianmu.workflow.event.process.ProcessStartedEvent;
import dev.jianmu.workflow.event.process.ProcessTerminatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * @author Ethan Liu
 * @class WorkflowEventHandler
 * @description 流程事件处理器
 * @create 2021-03-24 14:18
 */
@Component
@Slf4j
public class WorkflowInstanceEventHandler {
    private final WorkflowInternalApplication workflowInternalApplication;
    private final AsyncTaskInstanceInternalApplication asyncTaskInstanceInternalApplication;
    private final WorkerApplication workerApplication;
    private final ApplicationEventPublisher publisher;

    public WorkflowInstanceEventHandler(
            WorkflowInternalApplication workflowInternalApplication,
            AsyncTaskInstanceInternalApplication asyncTaskInstanceInternalApplication,
            WorkerApplication workerApplication,
            ApplicationEventPublisher publisher
    ) {
        this.workflowInternalApplication = workflowInternalApplication;
        this.asyncTaskInstanceInternalApplication = asyncTaskInstanceInternalApplication;
        this.workerApplication = workerApplication;
        this.publisher = publisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAggregateRootEvents(WorkflowInstance workflowInstance) {
        log.info("Get workflowInstance here -------------------------");
        workflowInstance.getUncommittedDomainEvents().forEach(event -> {
            log.info("publish {} here", event.getClass().getSimpleName());
            this.publisher.publishEvent(event);
        });
        workflowInstance.clear();
        log.info("-----------------------------------------------------");
    }

    @Async
    @EventListener
    public void handleProcessStartedEvent(ProcessStartedEvent event) {
        MDC.put("triggerId", event.getTriggerId());
        log.info("Get ProcessStartedEvent here -------------------------");
        log.info(event.toString());
        // 创建Workspace
        this.workerApplication.createWorkspace(event.getTriggerId());
        // 触发流程启动
        var workflowStartCmd = WorkflowStartCmd.builder()
                .triggerId(event.getTriggerId())
                .workflowRef(event.getWorkflowRef())
                .workflowVersion(event.getWorkflowVersion())
                .build();
        this.workflowInternalApplication.start(workflowStartCmd);
        log.info("-----------------------------------------------------");
    }

    @Async
    @EventListener
    public void handleProcessTerminatedEvent(ProcessTerminatedEvent event) {
        MDC.put("triggerId", event.getTriggerId());
        log.info("Get ProcessTerminatedEvent here -------------------------");
        log.info(event.toString());
        this.asyncTaskInstanceInternalApplication.terminateAll(event.getWorkflowInstanceId());
        log.info("-----------------------------------------------------");
    }

    @EventListener
    public void handleProcessEndedEvent(ProcessEndedEvent event) {
        MDC.put("triggerId", event.getTriggerId());
        log.info("Get ProcessEndedEvent here -------------------------");
        log.info(event.toString());
        this.workerApplication.cleanupWorkspace(event.getTriggerId());
        log.info("-----------------------------------------------------");
    }

    @EventListener
    public void handleProcessNotRunningEvent(ProcessNotRunningEvent event) {
        MDC.put("triggerId", event.getTriggerId());
        log.info("Get ProcessNotRunningEvent here -------------------------");
        log.info(event.toString());
        this.workerApplication.cleanupWorkspace(event.getTriggerId());
        log.info("-----------------------------------------------------");
    }
}
