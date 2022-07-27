package dev.jianmu.api.eventhandler;

import dev.jianmu.api.jwt.UserContextHolder;
import dev.jianmu.application.command.WorkflowStartCmd;
import dev.jianmu.application.service.GitRepoApplication;
import dev.jianmu.application.service.ProjectGroupApplication;
import dev.jianmu.application.service.TriggerApplication;
import dev.jianmu.application.service.internal.WorkflowInstanceInternalApplication;
import dev.jianmu.project.event.CreatedEvent;
import dev.jianmu.project.event.DeletedEvent;
import dev.jianmu.project.event.MovedEvent;
import dev.jianmu.project.event.TriggerEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Ethan Liu
 * @class DslEventHandler
 * @description Dsl事件处理器
 * @create 2021-04-23 17:21
 */
@Component
@Slf4j
public class ProjectEventHandler {
    private static final Map<String, Lock> PROJECT_LOCK_MAP = new ConcurrentHashMap<>();
    private final WorkflowInstanceInternalApplication workflowInstanceInternalApplication;
    private final TriggerApplication triggerApplication;
    private final ProjectGroupApplication projectGroupApplication;
    private final GitRepoApplication gitRepoApplication;
    private final UserContextHolder userContextHolder;

    public ProjectEventHandler(
            WorkflowInstanceInternalApplication workflowInstanceInternalApplication,
            TriggerApplication triggerApplication,
            ProjectGroupApplication projectGroupApplication,
            GitRepoApplication gitRepoApplication,
            UserContextHolder userContextHolder
    ) {
        this.workflowInstanceInternalApplication = workflowInstanceInternalApplication;
        this.triggerApplication = triggerApplication;
        this.projectGroupApplication = projectGroupApplication;
        this.gitRepoApplication = gitRepoApplication;
        this.userContextHolder = userContextHolder;
    }

    @EventListener
    public void handleTriggerEvent(TriggerEvent triggerEvent) {
        // 使用project id与WorkflowVersion作为triggerId,用于参数引用查询，参见WorkerApplication#getEnvironmentMap
        var cmd = WorkflowStartCmd.builder()
                .triggerId(triggerEvent.getTriggerId())
                .triggerType(triggerEvent.getTriggerType())
                .workflowRef(triggerEvent.getWorkflowRef())
                .workflowVersion(triggerEvent.getWorkflowVersion())
                .build();
        // TODO 集群环境需优化成分布式锁
        PROJECT_LOCK_MAP.putIfAbsent(triggerEvent.getProjectId(), new ReentrantLock());
        Lock lock = PROJECT_LOCK_MAP.get(triggerEvent.getProjectId());
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
        this.gitRepoApplication.addFlow(createdEvent.getProjectId(), createdEvent.getBranch(), this.userContextHolder.getSession().getAssociationId());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleProjectDelete(DeletedEvent deletedEvent) {
        // 项目删除事件, 删除相关的Trigger
        var encryptedToken = this.userContextHolder.getSession().getEncryptedToken();
        this.triggerApplication.deleteByProjectId(deletedEvent.getProjectId(), encryptedToken, deletedEvent.getAssociationId(), deletedEvent.getAssociationType());
        // 移除gitRepo中flow
        this.gitRepoApplication.removeFlow(deletedEvent.getProjectId(), deletedEvent.getAssociationId());
    }

    @TransactionalEventListener
    public void handleGroupUpdate(MovedEvent movedEvent) {
        // 移动项目到项目组事件
        this.projectGroupApplication.moveProject(movedEvent.getProjectId(), movedEvent.getProjectGroupId());
    }
}
