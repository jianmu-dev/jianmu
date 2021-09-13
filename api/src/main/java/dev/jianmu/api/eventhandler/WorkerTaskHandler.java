package dev.jianmu.api.eventhandler;

import dev.jianmu.application.service.EmbeddedWorkerApplication;
import dev.jianmu.worker.aggregate.Worker;
import dev.jianmu.worker.aggregate.WorkerTask;
import org.springframework.context.event.EventListener;
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
    public void embeddedWorker(WorkerTask workerTask) {
        if (workerTask.getType() == Worker.Type.EMBEDDED) {
            this.embeddedWorkerApplication.runTask(workerTask);
        }
    }

    @EventListener
    public void dockerWorker(WorkerTask workerTask) {
        if (workerTask.getType() == Worker.Type.DOCKER) {
            System.out.println("dockerWorker is here");
            System.out.println("dockerWorker got task: " + workerTask.getTaskInstanceId());
        }
    }

    @EventListener
    public void shellWorker(WorkerTask workerTask) {
        if (workerTask.getType() == Worker.Type.SHELL) {
            System.out.println("shellWorker is here");
            System.out.println("shellWorker got task: " + workerTask.getTaskInstanceId());
        }
    }
}
