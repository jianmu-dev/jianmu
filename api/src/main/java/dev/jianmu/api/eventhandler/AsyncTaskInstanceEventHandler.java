package dev.jianmu.api.eventhandler;

import dev.jianmu.application.command.NextNodeCmd;
import dev.jianmu.application.command.TaskActivatingCmd;
import dev.jianmu.application.service.internal.TaskInstanceInternalApplication;
import dev.jianmu.application.service.internal.WorkflowInstanceInternalApplication;
import dev.jianmu.application.service.internal.WorkflowInternalApplication;
import dev.jianmu.workflow.aggregate.process.AsyncTaskInstance;
import dev.jianmu.workflow.event.process.*;
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
 * @description 异步任务事件处理器
 * @create 2022-01-02 14:47
 */
@Component
@Slf4j
public class AsyncTaskInstanceEventHandler {
    private final WorkflowInstanceInternalApplication workflowInstanceInternalApplication;
    private final WorkflowInternalApplication workflowInternalApplication;
    private final TaskInstanceInternalApplication taskInstanceInternalApplication;
    private final ApplicationEventPublisher publisher;

    public AsyncTaskInstanceEventHandler(
            WorkflowInstanceInternalApplication workflowInstanceInternalApplication,
            WorkflowInternalApplication workflowInternalApplication,
            TaskInstanceInternalApplication taskInstanceInternalApplication,
            ApplicationEventPublisher publisher
    ) {
        this.workflowInstanceInternalApplication = workflowInstanceInternalApplication;
        this.workflowInternalApplication = workflowInternalApplication;
        this.taskInstanceInternalApplication = taskInstanceInternalApplication;
        this.publisher = publisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAggregateRootEvents(AsyncTaskInstance asyncTaskInstance) {
        log.info("Get AsyncTaskInstance here -------------------------");
        asyncTaskInstance.getUncommittedDomainEvents().forEach(event -> {
            log.info("publish {} here", event.getClass().getSimpleName());
            this.publisher.publishEvent(event);
        });
        asyncTaskInstance.clear();
        log.info("-----------------------------------------------------");
    }

    @Async
    @EventListener
    public void handleTaskActivatingEvent(TaskActivatingEvent event) {
        MDC.put("triggerId", event.getTriggerId());
        log.info("Get TaskActivatingEvent here -------------------------");
        log.info(event.toString());
        var cmd = TaskActivatingCmd.builder()
                .workflowRef(event.getWorkflowRef())
                .workflowVersion(event.getWorkflowVersion())
                .workflowInstanceId(event.getWorkflowInstanceId())
                .triggerId(event.getTriggerId())
                .nodeRef(event.getNodeRef())
                .nodeType(event.getNodeType())
                .asyncTaskInstanceId(event.getAsyncTaskInstanceId())
                .build();
        this.taskInstanceInternalApplication.create(cmd);
        log.info("-----------------------------------------------------");
    }

    @Async
    @EventListener
    public void handleTaskRetryEvent(TaskRetryEvent event) {
        log.info("Get TaskRetryEvent here -------------------------");
        var cmd = TaskActivatingCmd.builder()
                .workflowRef(event.getWorkflowRef())
                .workflowVersion(event.getWorkflowVersion())
                .workflowInstanceId(event.getWorkflowInstanceId())
                .triggerId(event.getTriggerId())
                .nodeRef(event.getNodeRef())
                .nodeType(event.getNodeType())
                .asyncTaskInstanceId(event.getAsyncTaskInstanceId())
                .build();
        log.info(event.toString());
        this.taskInstanceInternalApplication.create(cmd);
        log.info("-----------------------------------------------------");
    }

    @Async
    @EventListener
    public void handleTaskTerminatingEvent(TaskTerminatingEvent event) {
        MDC.put("triggerId", event.getTriggerId());
        log.info("Get TaskTerminatingEvent here -------------------------");
        log.info(event.toString());
        this.taskInstanceInternalApplication.terminate(event.getAsyncTaskInstanceId());
        log.info("-----------------------------------------------------");
    }

    @Async
    @EventListener
    public void handleTaskRunningEvent(TaskRunningEvent event) {
        MDC.put("triggerId", event.getTriggerId());
        log.info("Get TaskRunningEvent here -------------------------");
        log.info(event.toString());
        this.workflowInstanceInternalApplication.resume(event.getWorkflowInstanceId(), event.getNodeRef());
        log.info("-----------------------------------------------------");
    }

    @Async
    @EventListener
    public void handleTaskSucceededEvent(TaskSucceededEvent event) {
        MDC.put("triggerId", event.getTriggerId());
        log.info("Get TaskSucceededEvent here -------------------------");
        log.info(event.toString());
        var cmd = NextNodeCmd.builder()
                .triggerId(event.getTriggerId())
                .workflowRef(event.getWorkflowRef())
                .workflowVersion(event.getWorkflowVersion())
                .nodeRef(event.getNodeRef())
                .build();
        this.workflowInternalApplication.next(cmd);
        log.info("-----------------------------------------------------");
    }

    @Async
    @EventListener
    public void handleTaskIgnoredEvent(TaskIgnoredEvent event) {
        MDC.put("triggerId", event.getTriggerId());
        log.info("Get TaskIgnoredEvent here -------------------------");
        log.info(event.toString());
        var cmd = NextNodeCmd.builder()
                .triggerId(event.getTriggerId())
                .workflowRef(event.getWorkflowRef())
                .workflowVersion(event.getWorkflowVersion())
                .nodeRef(event.getNodeRef())
                .build();
        this.workflowInternalApplication.next(cmd);
        log.info("-----------------------------------------------------");
    }

    @EventListener
    public void handleTaskSuspendedEvent(TaskSuspendedEvent event) {
        MDC.put("triggerId", event.getTriggerId());
        log.info("Get TaskSuspendedEvent here -------------------------");
        log.info(event.toString());
        this.workflowInstanceInternalApplication.suspend(event.getWorkflowInstanceId());
        log.info("-----------------------------------------------------");
    }

    @EventListener
    public void handleTaskFailedEvent(TaskFailedEvent event) {
        MDC.put("triggerId", event.getTriggerId());
        log.info("Get TaskFailedEvent here -------------------------");
        log.info(event.toString());
        this.workflowInstanceInternalApplication.terminate(event.getWorkflowInstanceId());
        log.info("-----------------------------------------------------");
    }
}
