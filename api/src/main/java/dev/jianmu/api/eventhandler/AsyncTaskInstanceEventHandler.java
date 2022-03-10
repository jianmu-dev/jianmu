package dev.jianmu.api.eventhandler;

import dev.jianmu.application.command.NextNodeCmd;
import dev.jianmu.application.service.internal.TaskInstanceInternalApplication;
import dev.jianmu.application.service.internal.WorkerApplication;
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
    private final WorkerApplication workerApplication;
    private final ApplicationEventPublisher publisher;

    public AsyncTaskInstanceEventHandler(
            WorkflowInstanceInternalApplication workflowInstanceInternalApplication,
            WorkflowInternalApplication workflowInternalApplication,
            TaskInstanceInternalApplication taskInstanceInternalApplication,
            WorkerApplication workerApplication,
            ApplicationEventPublisher publisher
    ) {
        this.workflowInstanceInternalApplication = workflowInstanceInternalApplication;
        this.workflowInternalApplication = workflowInternalApplication;
        this.taskInstanceInternalApplication = taskInstanceInternalApplication;
        this.workerApplication = workerApplication;
        this.publisher = publisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAggregateRootEvents(AsyncTaskInstance asyncTaskInstance) {
        log.info("Get workflowInstance here -------------------------");
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
        this.taskInstanceInternalApplication.create(event);
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

    @EventListener
    public void handleTaskRunningEvent(TaskRunningEvent event) {
        MDC.put("triggerId", event.getTriggerId());
        log.info("Get TaskRunningEvent here -------------------------");
        log.info(event.toString());
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

    @EventListener
    public void handleTaskFailedEvent(TaskFailedEvent event) {
        MDC.put("triggerId", event.getTriggerId());
        log.info("Get TaskFailedEvent here -------------------------");
        log.info(event.toString());
        this.workflowInstanceInternalApplication.stop(event.getWorkflowInstanceId());
        this.workerApplication.cleanupWorkspace(event.getTriggerId());
        log.info("-----------------------------------------------------");
    }
}
