package dev.jianmu.api.runner;

import dev.jianmu.application.service.TriggerApplication;
import dev.jianmu.application.service.internal.TaskInstanceInternalApplication;
import dev.jianmu.task.event.TaskInstanceCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @class TriggerRunner
 * @description TriggerRunner
 * @author Ethan Liu
 * @create 2021-05-24 19:26
*/
@Component
@Slf4j
public class TriggerRunner implements ApplicationRunner {
    private final TriggerApplication triggerApplication;
    private final TaskInstanceInternalApplication taskInstanceInternalApplication;

    public TriggerRunner(
            TriggerApplication triggerApplication,
            TaskInstanceInternalApplication taskInstanceInternalApplication
    ) {
        this.triggerApplication = triggerApplication;
        this.taskInstanceInternalApplication = taskInstanceInternalApplication;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.triggerApplication.startTriggers();
//        this.resumeTasks();
    }

    @Async
    void resumeTasks() {
        var taskInstances = this.taskInstanceInternalApplication.findRunningTask();
        log.info("恢复仍在运行中的任务数量：{}", taskInstances.size());
        taskInstances.forEach(taskInstance -> {
            try {
                var event = TaskInstanceCreatedEvent.Builder.aTaskInstanceCreatedEvent()
                        .defKey(taskInstance.getDefKey())
                        .asyncTaskRef(taskInstance.getAsyncTaskRef())
                        .triggerId(taskInstance.getTriggerId())
                        .businessId(taskInstance.getBusinessId())
                        .taskInstanceId(taskInstance.getId())
                        .build();
//                this.workerApplication.dispatchTask(event, true);
                log.info("Task instance id: {}  ref: {} is resumed", taskInstance.getId(), taskInstance.getAsyncTaskRef());
            } catch (Exception e) {
                log.warn("Task instance id: {}  ref: {} is resume failed, due to: {}", taskInstance.getId(), taskInstance.getAsyncTaskRef(), e.getMessage());
                this.taskInstanceInternalApplication.executeFailed(taskInstance.getId());
            }
        });
    }
}
