package dev.jianmu.api.eventhandler;

import dev.jianmu.api.mapper.TaskResultMapper;
import dev.jianmu.application.service.TaskInstanceApplication;
import dev.jianmu.application.service.WorkerApplication;
import dev.jianmu.application.service.WorkflowInstanceApplication;
import dev.jianmu.infrastructure.docker.TaskFailedEvent;
import dev.jianmu.infrastructure.docker.TaskFinishedEvent;
import dev.jianmu.infrastructure.docker.TaskRunningEvent;
import dev.jianmu.task.aggregate.TaskInstance;
import dev.jianmu.task.event.TaskInstanceFailedEvent;
import dev.jianmu.task.event.TaskInstanceRunningEvent;
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
    private final TaskInstanceApplication taskInstanceApplication;
    private final WorkflowInstanceApplication workflowInstanceApplication;
    private final WorkerApplication workerApplication;

    @Inject
    public TaskInstanceEventHandler(
            TaskInstanceApplication taskInstanceApplication,
            WorkflowInstanceApplication workflowInstanceApplication,
            WorkerApplication workerApplication
    ) {
        this.taskInstanceApplication = taskInstanceApplication;
        this.workflowInstanceApplication = workflowInstanceApplication;
        this.workerApplication = workerApplication;
    }

    @EventListener
    public void handleTaskFinishedEvent(TaskFinishedEvent taskFinishedEvent) {
        // Worker执行状态事件通知任务上下文
        // TODO 运行状态需同步通知调度逻辑
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
        // Worker执行状态事件通知任务上下文
        // TODO 运行状态需同步通知调度逻辑
        this.taskInstanceApplication.running(taskRunningEvent.getTaskId());
    }

    @EventListener
    public void handleTaskFailedEvent(TaskFailedEvent taskFailedEvent) {
        // Worker执行状态事件通知任务上下文
        // TODO 运行状态需同步通知调度逻辑
        this.taskInstanceApplication.executeFailed(taskFailedEvent.getTaskId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskInstanceEvent(TaskInstance taskInstance) {
//        this.taskInstanceQueue.put(taskInstance);
        // 任务上下文抛出事件通知Worker
        // TODO 当前为直接触发,缺少调度逻辑，同时需要处理调度失败场景
        this.workerApplication.runTask(taskInstance);
        logger.info("Task instance id: {}  ref: {} is running", taskInstance.getId(), taskInstance.getAsyncTaskRef());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleTaskInstanceRunningEvent(TaskInstanceRunningEvent event) {
        // 任务上下文抛出事件通知流程上下文
        logger.info("get TaskInstanceRunningEvent: {}", event);
        this.workflowInstanceApplication.taskRun(event.getTaskInstanceId());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleTaskInstanceSucceedEvent(TaskInstanceSucceedEvent event) {
        // 任务上下文抛出事件通知流程上下文
        logger.info("get TaskInstanceSucceedEvent: {}", event);
        this.workflowInstanceApplication.taskSucceed(event.getTaskInstanceId());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleTaskInstanceFailedEvent(TaskInstanceFailedEvent event) {
        // 任务上下文抛出事件通知流程上下文
        logger.info("get TaskInstanceFailedEvent: {}", event);
        this.workflowInstanceApplication.taskFail(event.getTaskInstanceId());
    }
}
