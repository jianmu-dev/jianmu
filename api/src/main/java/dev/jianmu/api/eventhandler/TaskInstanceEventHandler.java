package dev.jianmu.api.eventhandler;

import dev.jianmu.api.dto.TaskResultDto;
import dev.jianmu.application.service.TaskInstanceApplication;
import dev.jianmu.application.service.WorkerApplication;
import dev.jianmu.application.service.WorkflowInstanceApplication;
import dev.jianmu.infrastructure.docker.TaskResult;
import dev.jianmu.infrastructure.messagequeue.TaskInstanceQueue;
import dev.jianmu.task.aggregate.InstanceStatus;
import dev.jianmu.task.aggregate.TaskInstance;
import dev.jianmu.task.event.TaskInstanceFailedEvent;
import dev.jianmu.task.event.TaskInstanceSucceedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
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
    private final WorkerApplication workerApplication;

    @Inject
    public TaskInstanceEventHandler(
            TaskInstanceQueue taskInstanceQueue,
            TaskInstanceApplication taskInstanceApplication,
            WorkflowInstanceApplication workflowInstanceApplication,
            WorkerApplication workerApplication
    ) {
        this.taskInstanceQueue = taskInstanceQueue;
        this.taskInstanceApplication = taskInstanceApplication;
        this.workflowInstanceApplication = workflowInstanceApplication;
        this.workerApplication = workerApplication;
    }

    @EventListener
    public void handleTaskStatusEvent(TaskResult taskResult) {
        var taskResultDto = TaskResultDto.builder().taskInstanceId(taskResult.getTaskId()).build();
        taskResultDto.setStatus(taskResult.getStatusCode() == 0 ? TaskResultDto.Status.SUCCEEDED : TaskResultDto.Status.FAILED);
        logger.info("update task {} status: {}", taskResultDto.getTaskInstanceId(), taskResultDto.getStatus());
        if (taskResultDto.getStatus() == TaskResultDto.Status.SUCCEEDED) {
            this.taskInstanceApplication.updateStatus(taskResultDto.getTaskInstanceId(), InstanceStatus.EXECUTION_SUCCEEDED);
        } else {
            this.taskInstanceApplication.updateStatus(taskResultDto.getTaskInstanceId(), InstanceStatus.EXECUTION_FAILED);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskInstanceEvent(TaskInstance taskInstance) {
//        this.taskInstanceQueue.put(taskInstance);
        this.workerApplication.runTask(taskInstance);
        this.taskInstanceApplication.updateStatus(taskInstance.getId(), InstanceStatus.RUNNING);
        this.workflowInstanceApplication.taskRun(taskInstance.getBusinessId(), taskInstance.getDefKey());
        logger.info("Task instance id: {}  key: {} is running", taskInstance.getId(), taskInstance.getDefKey());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleTaskInstanceSucceedEvent(TaskInstanceSucceedEvent event) {
        logger.info("get TaskInstanceSucceedEvent: {}", event);
        this.workflowInstanceApplication.taskSucceed(event.getBusinessId(), event.getDefKey());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleTaskInstanceFailedEvent(TaskInstanceFailedEvent event) {
        this.workflowInstanceApplication.taskFail(event.getBusinessId(), event.getDefKey());
    }
}
