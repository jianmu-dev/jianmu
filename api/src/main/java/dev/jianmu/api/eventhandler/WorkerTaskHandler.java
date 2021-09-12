package dev.jianmu.api.eventhandler;

import dev.jianmu.application.service.EmbeddedWorkerApplication;
import dev.jianmu.worker.aggregate.WorkerTask;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @class: WorkerTaskHandler
 * @description: WorkerTaskHandler
 * @author: Ethan Liu
 * @create: 2021-09-12 22:18
 **/
@Component
public class WorkerTaskHandler {
    private final EmbeddedWorkerApplication embeddedWorkerApplication;

    public WorkerTaskHandler(EmbeddedWorkerApplication embeddedWorkerApplication) {
        this.embeddedWorkerApplication = embeddedWorkerApplication;
    }

    @EventListener
    @Order(1)
    public void embeddedWorker(WorkerTask workerTask) {
        if (workerTask.getWorkerId().equals("embedded-worker-1")) {
            this.embeddedWorkerApplication.runTask(workerTask);
        }
    }
}
