package dev.jianmu.api.eventhandler;

import dev.jianmu.api.mapper.TaskResultMapper;
import dev.jianmu.application.service.internal.AsyncTaskInstanceInternalApplication;
import dev.jianmu.application.service.internal.TaskInstanceInternalApplication;
import dev.jianmu.application.service.internal.WorkerApplication;
import dev.jianmu.infrastructure.docker.TaskFailedEvent;
import dev.jianmu.infrastructure.docker.TaskFinishedEvent;
import dev.jianmu.infrastructure.docker.TaskRunningEvent;
import dev.jianmu.infrastructure.storage.MonitoringFileService;
import dev.jianmu.task.event.TaskInstanceCreatedEvent;
import dev.jianmu.task.event.TaskInstanceFailedEvent;
import dev.jianmu.task.event.TaskInstanceRunningEvent;
import dev.jianmu.task.event.TaskInstanceSucceedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * @author Ethan Liu
 * @class TaskInstanceEventHandler
 * @description 任务实例事件处理器
 * @create 2021-04-02 22:18
 */
@Component
public class TaskInstanceEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(TaskInstanceEventHandler.class);
    private final TaskInstanceInternalApplication taskInstanceInternalApplication;
    private final AsyncTaskInstanceInternalApplication asyncTaskInstanceInternalApplication;
    private final WorkerApplication workerApplication;
    private final MonitoringFileService monitoringFileService;

    public TaskInstanceEventHandler(
            TaskInstanceInternalApplication taskInstanceInternalApplication,
            AsyncTaskInstanceInternalApplication asyncTaskInstanceInternalApplication,
            WorkerApplication workerApplication,
            MonitoringFileService monitoringFileService) {
        this.taskInstanceInternalApplication = taskInstanceInternalApplication;
        this.asyncTaskInstanceInternalApplication = asyncTaskInstanceInternalApplication;
        this.workerApplication = workerApplication;
        this.monitoringFileService = monitoringFileService;
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
        this.monitoringFileService.clearCallbackByLogId(taskFinishedEvent.getTaskId());
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
        this.monitoringFileService.clearCallbackByLogId(taskFailedEvent.getTaskId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskInstanceEvent(TaskInstanceCreatedEvent event) {
        // 任务上下文抛出事件通知Worker
        this.workerApplication.dispatchTask(event, false);
        logger.info("Task instance id: {}  ref: {} is running", event.getTaskInstanceId(), event.getAsyncTaskRef());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleTaskInstanceRunningEvent(TaskInstanceRunningEvent event) {
        // 任务上下文抛出事件通知流程上下文
        logger.info("get TaskInstanceRunningEvent: {}", event);
        this.asyncTaskInstanceInternalApplication.run(event.getBusinessId());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleTaskInstanceSucceedEvent(TaskInstanceSucceedEvent event) {
        // 任务上下文抛出事件通知流程上下文
        logger.info("get TaskInstanceSucceedEvent: {}", event);
        this.asyncTaskInstanceInternalApplication.succeed(event.getBusinessId());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleTaskInstanceFailedEvent(TaskInstanceFailedEvent event) {
        // 任务上下文抛出事件通知流程上下文
        logger.info("get TaskInstanceFailedEvent: {}", event);
        this.asyncTaskInstanceInternalApplication.stop(event.getTriggerId(), event.getBusinessId());
    }
}
