package dev.jianmu.api.eventhandler;

import dev.jianmu.api.mapper.TaskResultMapper;
import dev.jianmu.application.service.internal.TaskInstanceInternalApplication;
import dev.jianmu.application.service.internal.WorkerApplication;
import dev.jianmu.application.service.internal.WorkflowInstanceInternalApplication;
import dev.jianmu.infrastructure.docker.TaskFailedEvent;
import dev.jianmu.infrastructure.docker.TaskFinishedEvent;
import dev.jianmu.infrastructure.docker.TaskRunningEvent;
import dev.jianmu.task.aggregate.TaskInstance;
import dev.jianmu.task.event.TaskInstanceFailedEvent;
import dev.jianmu.task.event.TaskInstanceRunningEvent;
import dev.jianmu.task.event.TaskInstanceSucceedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * @class: TaskInstanceEventHandler
 * @description: 任务实例事件处理器
 * @author: Ethan Liu
 * @create: 2021-04-02 22:18
 **/
@Component
public class TaskInstanceEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(TaskInstanceEventHandler.class);
    private final TaskInstanceInternalApplication taskInstanceInternalApplication;
    private final WorkflowInstanceInternalApplication workflowInstanceInternalApplication;
    private final WorkerApplication workerApplication;

    public TaskInstanceEventHandler(
            TaskInstanceInternalApplication taskInstanceInternalApplication,
            WorkflowInstanceInternalApplication workflowInstanceInternalApplication,
            WorkerApplication workerApplication
    ) {
        this.taskInstanceInternalApplication = taskInstanceInternalApplication;
        this.workflowInstanceInternalApplication = workflowInstanceInternalApplication;
        this.workerApplication = workerApplication;
    }

    @EventListener
    public void handleTaskFinishedEvent(TaskFinishedEvent taskFinishedEvent) {
        // Worker执行状态事件通知任务上下文
        // TODO 运行状态需同步通知调度逻辑
        MDC.put("triggerId", taskFinishedEvent.getTriggerId());
        var taskResultDto = TaskResultMapper.INSTANCE.toTaskResultDto(taskFinishedEvent);
        if (taskResultDto.isSucceeded()) {
            this.taskInstanceInternalApplication.executeSucceeded(
                    taskResultDto.getTaskInstanceId(), taskResultDto.getResultFile()
            );
        } else {
            this.taskInstanceInternalApplication.executeFailed(taskResultDto.getTaskInstanceId());
        }
    }

    @EventListener
    public void handleTaskRunningEvent(TaskRunningEvent taskRunningEvent) {
        // Worker执行状态事件通知任务上下文
        // TODO 运行状态需同步通知调度逻辑
        this.taskInstanceInternalApplication.running(taskRunningEvent.getTaskId());
    }

    @EventListener
    public void handleTaskFailedEvent(TaskFailedEvent taskFailedEvent) {
        // Worker执行状态事件通知任务上下文
        // TODO 运行状态需同步通知调度逻辑
        MDC.put("triggerId", taskFailedEvent.getTriggerId());
        logger.info("task {} is failed, due to: {}", taskFailedEvent.getTaskId(), taskFailedEvent.getErrorMsg());
        this.taskInstanceInternalApplication.executeFailed(taskFailedEvent.getTaskId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskInstanceEvent(TaskInstance taskInstance) {
//        this.taskInstanceQueue.put(taskInstance);
        // 任务上下文抛出事件通知Worker
        this.workerApplication.dispatchTask(taskInstance, false);
        logger.info("Task instance id: {}  ref: {} is running", taskInstance.getId(), taskInstance.getAsyncTaskRef());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleTaskInstanceRunningEvent(TaskInstanceRunningEvent event) {
        // 任务上下文抛出事件通知流程上下文
        logger.info("get TaskInstanceRunningEvent: {}", event);
        this.workflowInstanceInternalApplication.taskRun(event.getTaskInstanceId());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleTaskInstanceSucceedEvent(TaskInstanceSucceedEvent event) {
        // 任务上下文抛出事件通知流程上下文
        logger.info("get TaskInstanceSucceedEvent: {}", event);
        this.workflowInstanceInternalApplication.taskSucceed(event.getTaskInstanceId());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleTaskInstanceFailedEvent(TaskInstanceFailedEvent event) {
        // 任务上下文抛出事件通知流程上下文
        logger.info("get TaskInstanceFailedEvent: {}", event);
        this.workflowInstanceInternalApplication.taskFail(event.getTaskInstanceId());
    }

    @EventListener
    @Async
    public void handleApplicationReadyEvent(ApplicationReadyEvent event) {
        var taskInstances = this.taskInstanceInternalApplication.findRunningTask();
        logger.info("恢复仍在运行中的任务数量：{}", taskInstances.size());
        taskInstances.forEach(taskInstance -> {
            this.workerApplication.dispatchTask(taskInstance, true);
            logger.info("Task instance id: {}  ref: {} is resumed", taskInstance.getId(), taskInstance.getAsyncTaskRef());
        });
    }
}
