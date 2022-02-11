package dev.jianmu.api.eventhandler;

import dev.jianmu.application.service.internal.EmbeddedWorkerApplication;
import dev.jianmu.node.definition.event.NodeDeletedEvent;
import dev.jianmu.node.definition.event.NodeUpdatedEvent;
import dev.jianmu.worker.aggregate.Worker;
import dev.jianmu.worker.aggregate.WorkerTask;
import dev.jianmu.worker.event.CleanupWorkspaceEvent;
import dev.jianmu.worker.event.CreateWorkspaceEvent;
import dev.jianmu.worker.event.TerminateTaskEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author Ethan Liu
 * @class WorkerEventHandler
 * @description WorkerEventHandler
 * @create 2021-09-12 22:18
 */
@Component
@Slf4j
public class WorkerEventHandler {
    private final EmbeddedWorkerApplication embeddedWorkerApplication;

    public WorkerEventHandler(EmbeddedWorkerApplication embeddedWorkerApplication) {
        this.embeddedWorkerApplication = embeddedWorkerApplication;
    }

    @EventListener
    public void createWorkspaceEventHandle(CreateWorkspaceEvent event) {
        this.embeddedWorkerApplication.createVolume(event);
    }

    @EventListener
    public void cleanupWorkspaceEventHandle(CleanupWorkspaceEvent cleanupWorkspaceEvent) {
        this.embeddedWorkerApplication.deleteVolume(cleanupWorkspaceEvent.getWorkspaceName());
    }

    @EventListener
    public void handleTerminateTaskEvent(TerminateTaskEvent terminateTaskEvent) {
        log.info("docker worker terminate task id: {}", terminateTaskEvent.getTaskInstanceId());
        this.embeddedWorkerApplication.terminateTask(terminateTaskEvent.getTriggerId(), terminateTaskEvent.getTaskInstanceId());
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
