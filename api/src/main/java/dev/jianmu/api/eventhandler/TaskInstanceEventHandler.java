package dev.jianmu.api.eventhandler;

import dev.jianmu.api.mapper.TaskResultMapper;
import dev.jianmu.application.service.TaskInstanceApplication;
import dev.jianmu.application.service.WorkerApplication;
import dev.jianmu.application.service.WorkflowInstanceApplication;
import dev.jianmu.infrastructure.docker.TaskFinishedEvent;
import dev.jianmu.infrastructure.docker.TaskRunningEvent;
import dev.jianmu.infrastructure.messagequeue.TaskInstanceQueue;
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
    public void handleTaskFinishedEvent(TaskFinishedEvent taskFinishedEvent) {
        var taskResultDto = TaskResultMapper.INSTANCE.toTaskResultDto(taskFinishedEvent);
        if (taskResultDto.isSucceeded()) {
            this.taskInstanceApplication.executeSucceeded(
                    taskResultDto.getTaskInstanceId(), taskResultDto.getResultFile()
            );
        } else {
            this.taskInstanceApplication.executeFailed(taskResultDto.getTaskInstanceId());
        }
    }

    @EventListener
    public void handleTaskRunningEvent(TaskRunningEvent taskRunningEvent) {
        // TODO 运行状态需同步通知调度逻辑
        this.taskInstanceApplication.running(taskRunningEvent.getTaskId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskInstanceEvent(TaskInstance taskInstance) {
//        this.taskInstanceQueue.put(taskInstance);
        // TODO 缺少调度逻辑，同时需要处理调度失败场景
        this.workerApplication.runTask(taskInstance);
        this.workflowInstanceApplication.taskRun(taskInstance.getBusinessId(), taskInstance.getAsyncTaskRef());
        logger.info("Task instance id: {}  ref: {} is running", taskInstance.getId(), taskInstance.getAsyncTaskRef());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleTaskInstanceSucceedEvent(TaskInstanceSucceedEvent event) {
        logger.info("get TaskInstanceSucceedEvent: {}", event);
        this.workflowInstanceApplication.taskSucceed(event.getBusinessId(), event.getAsyncTaskRef());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleTaskInstanceFailedEvent(TaskInstanceFailedEvent event) {
        this.workflowInstanceApplication.taskFail(event.getBusinessId(), event.getAsyncTaskRef());
    }
}
