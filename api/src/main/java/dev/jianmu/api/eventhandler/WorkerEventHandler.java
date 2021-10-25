package dev.jianmu.api.eventhandler;

import dev.jianmu.application.service.internal.EmbeddedWorkerApplication;
import dev.jianmu.hub.intergration.event.NodeDeletedEvent;
import dev.jianmu.hub.intergration.event.NodeUpdatedEvent;
import dev.jianmu.worker.aggregate.Worker;
import dev.jianmu.worker.aggregate.WorkerTask;
import dev.jianmu.worker.event.CleanupWorkspaceEvent;
import dev.jianmu.worker.event.CreateWorkspaceEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @class: WorkerEventHandler
 * @description: WorkerEventHandler
 * @author: Ethan Liu
 * @create: 2021-09-12 22:18
 **/
@Component
@Slf4j
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
            log.info("embedded docker worker running task id: {}", workerTask.getTaskInstanceId());
            log.info("embedded docker worker running task type: {}", workerTask.getDefKey());
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
    public void nodeDeletedEventHandle(NodeDeletedEvent event) {
        log.info("get NodeDeletedEvent here");
        log.info("删除节点定义版本: {}/{}:{}", event.getOwnerRef(), event.getRef(), event.getVersion());
        this.embeddedWorkerApplication.deleteImage(event);
    }

    @EventListener
    public void nodeUpdatedEventHandle(NodeUpdatedEvent event) {
        log.info("get NodeUpdatedEvent here");
        log.info("更新节点定义版本: {}/{}:{}", event.getOwnerRef(), event.getRef(), event.getVersion());
        this.embeddedWorkerApplication.updateImage(event);
    }

    @EventListener
    public void shellWorkerTask(WorkerTask workerTask) {
        if (workerTask.getType() == Worker.Type.SHELL) {
            System.out.println("shellWorker is here");
            System.out.println("shellWorker got task: " + workerTask.getTaskInstanceId());
        }
    }
}
