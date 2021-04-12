package dev.jianmu.api.eventhandler;

import dev.jianmu.application.service.TaskInstanceApplication;
import dev.jianmu.application.service.WorkflowInstanceApplication;
import dev.jianmu.workflow.aggregate.AggregateRoot;
import dev.jianmu.workflow.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.inject.Inject;

/**
 * @class: WorkflowEventHandler
 * @description: 流程事件处理器
 * @author: Ethan Liu
 * @create: 2021-03-24 14:18
 **/
@Component
public class WorkflowEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowEventHandler.class);

    private final WorkflowInstanceApplication instanceApplication;
    private final TaskInstanceApplication taskInstanceApplication;
    private final ApplicationEventPublisher publisher;

    @Inject
    public WorkflowEventHandler(WorkflowInstanceApplication instanceApplication, TaskInstanceApplication taskInstanceApplication, ApplicationEventPublisher publisher) {
        this.instanceApplication = instanceApplication;
        this.taskInstanceApplication = taskInstanceApplication;
        this.publisher = publisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAggregateRootEvents(AggregateRoot aggregateRoot) {
        logger.info("Get AggregateRoot here -------------------------");
        aggregateRoot.getUncommittedDomainEvents().forEach(event -> {
            logger.info("publishEvent here");
            this.publisher.publishEvent(event);
        });
        logger.info("-----------------------------------------------------");
    }

    @Async
    @EventListener
    public void handleTaskActivatingEvent(TaskActivatingEvent event) {
        logger.info("Get TaskActivatingEvent here -------------------------");
        logger.info(event.getName());
        logger.info(event.getNodeRef());
        logger.info(event.getWorkflowInstanceId());
        logger.info(event.getTriggerId());
        this.taskInstanceApplication.create(event.getWorkflowInstanceId(), event.getTriggerId(), event.getNodeRef());
        logger.info("-----------------------------------------------------");
    }

    @EventListener
    public void handleTaskTerminatingEvent(TaskTerminatingEvent event) {
        logger.info("Get TaskTerminatingEvent here -------------------------");
        logger.info(event.getName());
        logger.info(event.getNodeRef());
        logger.info("-----------------------------------------------------");
    }

    @EventListener
    public void handleTaskRunningEvent(TaskRunningEvent event) {
        logger.info("Get TaskRunningEvent here -------------------------");
        logger.info(event.getName());
        logger.info(event.getNodeRef());
        logger.info("-----------------------------------------------------");
    }

    @EventListener
    public void handleTaskSucceededEvent(TaskSucceededEvent event) {
        logger.info("Get TaskSucceededEvent here -------------------------");
        logger.info(event.getName());
        logger.info(event.getNodeRef());
        logger.info("-----------------------------------------------------");
    }

    @EventListener
    public void handleTaskFailedEvent(TaskFailedEvent event) {
        logger.info("Get TaskFailedEvent here -------------------------");
        logger.info(event.getName());
        logger.info(event.getNodeRef());
        logger.info("-----------------------------------------------------");
    }

    @Async
    @EventListener
    public void handleNodeActivatingEvent(NodeActivatingEvent event) {
        logger.info("Get NodeActivatingEvent here -------------------------");
        logger.info(event.getNodeRef());
        this.instanceApplication.activateNode(event.getWorkflowInstanceId(), event.getNodeRef());
        logger.info("handle NodeActivatingEvent end-----------------------------------------------------");
    }

    @EventListener
    public void handleWorkflowStartEvent(WorkflowStartEvent event) {
        logger.info("Get WorkflowStartEvent here -------------------------");
        logger.info(event.getName());
        logger.info(event.getWorkflowInstanceId());
        logger.info("-----------------------------------------------------");
    }

    @EventListener
    public void handleWorkflowEndEvent(WorkflowEndEvent event) {
        logger.info("Get WorkflowEndEvent here -------------------------");
        logger.info(event.getName());
        logger.info(event.getWorkflowInstanceId());
        logger.info("-----------------------------------------------------");
    }
}
