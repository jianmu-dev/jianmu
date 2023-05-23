package dev.jianmu.api.eventhandler;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import dev.jianmu.application.command.WorkflowStartCmd;
import dev.jianmu.application.service.GitRepoApplication;
import dev.jianmu.application.service.ProjectGroupApplication;
import dev.jianmu.application.service.TriggerApplication;
import dev.jianmu.application.service.internal.WorkflowInstanceInternalApplication;
import dev.jianmu.application.util.AssociationUtil;
import dev.jianmu.infrastructure.lock.DistributedLock;
import dev.jianmu.project.event.CreatedEvent;
import dev.jianmu.project.event.DeletedEvent;
import dev.jianmu.project.event.TrashEvent;
import dev.jianmu.project.event.MovedEvent;
import dev.jianmu.project.event.TriggerEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Ethan Liu
 * @class DslEventHandler
 * @description Dsl事件处理器
 * @create 2021-04-23 17:21
 */
@Component
@Slf4j
public class ProjectEventHandler {
    private final WorkflowInstanceInternalApplication workflowInstanceInternalApplication;
    private final ProjectApplication projectApplication;
    private final TriggerApplication triggerApplication;
    private final ProjectGroupApplication projectGroupApplication;
    private final GitRepoApplication gitRepoApplication;
    private final DistributedLock distributedLock;

    public ProjectEventHandler(
            WorkflowInstanceInternalApplication workflowInstanceInternalApplication,
            ProjectApplication projectApplication,
            TriggerApplication triggerApplication,
            ProjectGroupApplication projectGroupApplication,
            GitRepoApplication gitRepoApplication,
            DistributedLock distributedLock
    ) {
        this.workflowInstanceInternalApplication = workflowInstanceInternalApplication;
        this.projectApplication = projectApplication;
        this.triggerApplication = triggerApplication;
        this.projectGroupApplication = projectGroupApplication;
        this.gitRepoApplication = gitRepoApplication;
        this.distributedLock = distributedLock;
    }

    @EventListener
    public void handleTriggerEvent(TriggerEvent triggerEvent) {
        // 使用project id与WorkflowVersion作为triggerId,用于参数引用查询，参见WorkerApplication#getEnvironmentMap
        var cmd = WorkflowStartCmd.builder()
            .triggerId(triggerEvent.getTriggerId())
            .triggerType(triggerEvent.getTriggerType())
            .workflowRef(triggerEvent.getWorkflowRef())
            .workflowVersion(triggerEvent.getWorkflowVersion())
            .occurredTime(triggerEvent.getOccurredTime())
            .build();
        var lock = this.distributedLock.getLock(triggerEvent.getProjectId());
        lock.lock();
        try {
            this.workflowInstanceInternalApplication.create(cmd, triggerEvent.getProjectId());
        } finally {
            lock.unlock();
        }
    }

    @EventListener
    // 项目创建事件
    public void handleProjectCreate(CreatedEvent createdEvent) {
        // 添加gitRepo中的flow
        if (AssociationUtil.AssociationType.GIT_REPO.name().equals(createdEvent.getAssociationType())) {
            this.gitRepoApplication.addFlow(createdEvent.getProjectId(), createdEvent.getBranch(), createdEvent.getAssociationId());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleProjectDelete(DeletedEvent deletedEvent) {
        // 项目删除事件, 删除相关的Trigger
        this.triggerApplication.deleteByProjectId(deletedEvent.getProjectId(), deletedEvent.getUserId(), deletedEvent.getAssociationId(), deletedEvent.getAssociationType());
        // 移除gitRepo中flow
        if (AssociationUtil.AssociationType.GIT_REPO.name().equals(deletedEvent.getAssociationType())) {
            this.gitRepoApplication.removeFlow(deletedEvent.getProjectId(), deletedEvent.getAssociationId());
        }
    }

    @TransactionalEventListener
    public void handleGroupUpdate(MovedEvent movedEvent) {
        if (movedEvent.getProjectGroupId() == null) {
            return;
        }
        // 移动项目到项目组事件
        this.projectGroupApplication.moveProject(movedEvent.getProjectId(), movedEvent.getProjectGroupId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlerFileDelete(TrashEvent event) {
        // 清理项目数据
        this.projectApplication.trashProject(event.getProjectId());
    }
}
