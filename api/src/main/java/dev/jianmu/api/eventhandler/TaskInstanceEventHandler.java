package dev.jianmu.api.eventhandler;

import dev.jianmu.application.service.TaskInstanceApplication;
import dev.jianmu.application.service.WorkflowInstanceApplication;
import dev.jianmu.infrastructure.messagequeue.TaskInstanceQueue;
import dev.jianmu.task.aggregate.TaskInstance;
import dev.jianmu.task.aggregate.InstanceStatus;
import dev.jianmu.task.event.TaskInstanceFailedEvent;
import dev.jianmu.task.event.TaskInstanceSucceedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.inject.Inject;

/**
 * @class: TaskInstanceEventHandler
 * @description: 任务实例事件处理器
 * @author: Ethan Liu
 * @create: 2021-04-02 22:18
 **/
@Component
public class TaskInstanceEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(TaskInstanceEventHandler.class);
    private final TaskInstanceQueue taskInstanceQueue;
    private final TaskInstanceApplication taskInstanceApplication;
    private final WorkflowInstanceApplication workflowInstanceApplication;

    @Inject
    public TaskInstanceEventHandler(TaskInstanceQueue taskInstanceQueue, TaskInstanceApplication taskInstanceApplication, WorkflowInstanceApplication workflowInstanceApplication) {
        this.taskInstanceQueue = taskInstanceQueue;
        this.taskInstanceApplication = taskInstanceApplication;
        this.workflowInstanceApplication = workflowInstanceApplication;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskInstanceEvent(TaskInstance taskInstance) {
        this.taskInstanceQueue.put(taskInstance);
        this.taskInstanceApplication.updateStatus(taskInstance.getId(), InstanceStatus.RUNNING);
        this.workflowInstanceApplication.taskRun(taskInstance.getBusinessId(), taskInstance.getDefKey() + taskInstance.getDefVersion());
        logger.info("Task instance id: {}  name: {} is running", taskInstance.getId(), taskInstance.getName());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleTaskInstanceSucceedEvent(TaskInstanceSucceedEvent event) {
        logger.info("get TaskInstanceSucceedEvent: {}",event);
        this.workflowInstanceApplication.taskSucceed(event.getBusinessId(), event.getDefKey() + event.getDefVersion());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleTaskInstanceFailedEvent(TaskInstanceFailedEvent event) {
        this.workflowInstanceApplication.taskFail(event.getBusinessId(), event.getDefKey() + event.getDefVersion());
    }
}
