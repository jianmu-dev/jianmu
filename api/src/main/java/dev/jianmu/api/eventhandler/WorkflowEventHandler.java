package dev.jianmu.api.eventhandler;

import dev.jianmu.application.command.ActivateNodeCmd;
import dev.jianmu.application.command.AsyncTaskActivatingCmd;
import dev.jianmu.application.command.SkipNodeCmd;
import dev.jianmu.application.service.internal.AsyncTaskInstanceInternalApplication;
import dev.jianmu.application.service.internal.WorkflowInstanceInternalApplication;
import dev.jianmu.application.service.internal.WorkflowInternalApplication;
import dev.jianmu.workflow.aggregate.definition.Workflow;
import dev.jianmu.workflow.event.definition.*;
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
 * @class AsyncTaskInstanceEventHandler
 * @description AsyncTaskInstanceEventHandler
 * @create 2022-01-01 10:45
 */
@Component
@Slf4j
public class WorkflowEventHandler {
    private final WorkflowInternalApplication workflowInternalApplication;
    private final WorkflowInstanceInternalApplication workflowInstanceInternalApplication;
    private final AsyncTaskInstanceInternalApplication asyncTaskInstanceInternalApplication;
    private final ApplicationEventPublisher publisher;

    public WorkflowEventHandler(
            WorkflowInternalApplication workflowInternalApplication,
            WorkflowInstanceInternalApplication workflowInstanceInternalApplication,
            AsyncTaskInstanceInternalApplication asyncTaskInstanceInternalApplication,
            ApplicationEventPublisher publisher
    ) {
        this.workflowInternalApplication = workflowInternalApplication;
        this.workflowInstanceInternalApplication = workflowInstanceInternalApplication;
        this.asyncTaskInstanceInternalApplication = asyncTaskInstanceInternalApplication;
        this.publisher = publisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProcessEvents(Workflow workflow) {
        log.info("Get Workflow Events here -------------------------");
        workflow.getUncommittedDomainEvents().forEach(event -> {
            log.info("publish {} here", event.getClass().getSimpleName());
            this.publisher.publishEvent(event);
        });
        workflow.clear();
        log.info("-----------------------------------------------------");
    }

    @EventListener
    public void handleWorkflowStartEvent(WorkflowStartEvent event) {
        MDC.put("triggerId", event.getTriggerId());
        log.info("Get WorkflowStartEvent here -------------------------");
        log.info(event.toString());
        log.info("handle WorkflowStartEvent end-----------------------------------------------------");
    }

    @Async
    @EventListener
    public void handleNodeActivatingEvent(NodeActivatingEvent event) {
        MDC.put("triggerId", event.getTriggerId());
        log.info("Get NodeActivatingEvent here -------------------------");
        log.info(event.toString());
        var cmd = ActivateNodeCmd.builder()
                .triggerId(event.getTriggerId())
                .workflowRef(event.getWorkflowRef())
                .workflowVersion(event.getWorkflowVersion())
                .nodeRef(event.getNodeRef())
                .sender(event.getSender())
                .build();
        this.workflowInternalApplication.activateNode(cmd);
        log.info("handle NodeActivatingEvent end-----------------------------------------------------");
    }

    @Async
    @EventListener
    public void handleAsyncTaskActivatingEvent(AsyncTaskActivatingEvent event) {
        MDC.put("triggerId", event.getTriggerId());
        log.info("Get AsyncTaskActivatingEvent here -------------------------");
        log.info(event.toString());
        var cmd = AsyncTaskActivatingCmd.builder()
                .triggerId(event.getTriggerId())
                .workflowRef(event.getWorkflowRef())
                .workflowVersion(event.getWorkflowVersion())
                .asyncTaskRef(event.getNodeRef())
                .asyncTaskType(event.getNodeType())
                .version(event.getVersion())
                .build();
        this.workflowInstanceInternalApplication.statusCheck(event.getTriggerId());
        this.asyncTaskInstanceInternalApplication.activate(cmd);
        log.info("handle AsyncTaskActivatingEvent end-----------------------------------------------------");
    }

    @Async
    @EventListener
    public void handleNodeSucceedEvent(NodeSucceedEvent event) {
        MDC.put("triggerId", event.getTriggerId());
        log.info("Get NodeSucceedEvent here -------------------------");
        log.info(event.toString());
        this.asyncTaskInstanceInternalApplication.nodeSucceed(event.getTriggerId(), event.getNodeRef(), event.getNextTarget(), event.getVersion());
        log.info("handle NodeSucceedEvent end-----------------------------------------------------");
    }

    @Async
    @EventListener
    public void handleNodeSkipEvent(NodeSkipEvent event) {
        MDC.put("triggerId", event.getTriggerId());
        log.info("Get NodeSkipEvent here -------------------------");
        log.info(event.toString());
        var cmd = SkipNodeCmd.builder()
                .triggerId(event.getTriggerId())
                .workflowRef(event.getWorkflowRef())
                .workflowVersion(event.getWorkflowVersion())
                .nodeRef(event.getNodeRef())
                .sender(event.getSender())
                .build();
        this.workflowInternalApplication.skipNode(cmd);
        log.info("handle NodeSkipEvent end-----------------------------------------------------");
    }

    @Async
    @EventListener
    public void handleWorkflowEndEvent(WorkflowEndEvent event) {
        MDC.put("triggerId", event.getTriggerId());
        log.info("Get WorkflowEndEvent here -------------------------");
        log.info(event.toString());
        this.workflowInstanceInternalApplication.end(event.getTriggerId());
        log.info("handle WorkflowEndEvent end-----------------------------------------------------");
    }
}
