package dev.jianmu.api.eventhandler;

import dev.jianmu.application.service.EmbeddedWorkerApplication;
import dev.jianmu.worker.aggregate.Worker;
import dev.jianmu.worker.aggregate.WorkerTask;
import dev.jianmu.worker.event.CleanupWorkspaceEvent;
import dev.jianmu.worker.event.CreateWorkspaceEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @class: WorkerEventHandler
 * @description: WorkerEventHandler
 * @author: Ethan Liu
 * @create: 2021-09-12 22:18
 **/
@Component
public class WorkerEventHandler {
    private final EmbeddedWorkerApplication embeddedWorkerApplication;

    public WorkerEventHandler(EmbeddedWorkerApplication embeddedWorkerApplication) {
        this.embeddedWorkerApplication = embeddedWorkerApplication;
    }

    @EventListener
    public void createWorkspaceEventHandle(CreateWorkspaceEvent createWorkspaceEvent) {
        this.embeddedWorkerApplication.createVolume(createWorkspaceEvent.getWorkspaceName());
    }

    @EventListener
    public void cleanupWorkspaceEventHandle(CleanupWorkspaceEvent cleanupWorkspaceEvent) {
        this.embeddedWorkerApplication.deleteVolume(cleanupWorkspaceEvent.getWorkspaceName());
    }

    @EventListener
    public void embeddedWorkerTask(WorkerTask workerTask) {
        if (workerTask.getType() == Worker.Type.EMBEDDED) {
            this.embeddedWorkerApplication.runTask(workerTask);
        }
    }

    @EventListener
    public void dockerWorkerTask(WorkerTask workerTask) {
        if (workerTask.getType() == Worker.Type.DOCKER) {
            System.out.println("dockerWorker is here");
            System.out.println("dockerWorker got task: " + workerTask.getTaskInstanceId());
        }
    }

    @EventListener
    public void shellWorkerTask(WorkerTask workerTask) {
        if (workerTask.getType() == Worker.Type.SHELL) {
            System.out.println("shellWorker is here");
            System.out.println("shellWorker got task: " + workerTask.getTaskInstanceId());
        }
    }
}
