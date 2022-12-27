package dev.jianmu.workflow.aggregate.process;

import dev.jianmu.event.impl.AsyncTaskInstanceStatusUpdatedEvent;
import dev.jianmu.workflow.aggregate.AggregateRoot;
import dev.jianmu.workflow.event.process.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author Ethan Liu
 * @program: workflow
 * @description 异步任务执行实例
 * @create 2021-01-21 20:45
 */
public class AsyncTaskInstance extends AggregateRoot {
    // ID
    private String id;
    // 触发器ID
    private String triggerId;
    // 流程定义唯一引用名称
    private String workflowRef;
    // 流程定义版本
    private String workflowVersion;
    // 流程定义版本
    private String workflowInstanceId;
    // 显示名称
    private String name;
    // 描述
    private String description;
    // 运行状态
    private TaskStatus status = TaskStatus.INIT;
    // 错误处理模式
    private FailureMode failureMode = FailureMode.SUSPEND;
    // 任务定义唯一引用名称
    private String asyncTaskRef;
    // 任务定义类型
    private String asyncTaskType;
    // 完成次数计数，从0开始
    private int serialNo;
    // 下一个要触发的节点, TODO 3.0需要重构
    private String nextTarget;
    // 创建时间
    private LocalDateTime activatingTime = LocalDateTime.now();
    // 开始时间
    private LocalDateTime startTime;
    // 结束时间
    private LocalDateTime endTime;
    // 乐观锁字段
    private int version;

    public boolean isNextTarget(String ref) {
        if (nextTarget == null) {
            return false;
        }
        return nextTarget.equals(ref);
    }

    public void activating() {
        this.version++;
        this.activatingTime = LocalDateTime.now();
        // 发布任务激活事件并返回
        TaskActivatingEvent taskActivatingEvent = TaskActivatingEvent.Builder.aTaskActivatingEvent()
                .nodeRef(this.asyncTaskRef)
                .triggerId(this.triggerId)
                .workflowInstanceId(this.workflowInstanceId)
                .asyncTaskInstanceId(this.id)
                .workflowRef(this.workflowRef)
                .workflowVersion(this.workflowVersion)
                .nodeType(this.asyncTaskType)
                .build();
        this.raiseEvent(taskActivatingEvent);
    }

    // 异步任务开始执行
    public void waiting() {
        this.status = TaskStatus.WAITING;
        this.startTime = null;
        this.endTime = null;
    }

    // 异步任务开始执行
    public void run() {
        var preStatus = this.status;
        this.status = TaskStatus.RUNNING;
        this.startTime = LocalDateTime.now();
        this.endTime = null;
        // 发布任务开始执行事件
        this.raiseEvent(
                TaskRunningEvent.Builder.aTaskRunningEvent()
                        .nodeRef(this.asyncTaskRef)
                        .triggerId(this.triggerId)
                        .workflowInstanceId(this.workflowInstanceId)
                        .asyncTaskInstanceId(this.id)
                        .workflowRef(this.workflowRef)
                        .workflowVersion(this.workflowVersion)
                        .nodeType(this.asyncTaskType)
                        .preStatus(preStatus)
                        .build()
        );
        // 发布状态变更事件
        this.publishStatusUpdatedEvent();
    }

    public void retry() {
        if (this.status != TaskStatus.SUSPENDED) {
            throw new RuntimeException("非挂起状态的任务不能重试");
        }
        this.version++;
        this.activatingTime = LocalDateTime.now();
        var taskRetryEvent = TaskRetryEvent.Builder.aTaskRetryEvent()
                .nodeRef(this.asyncTaskRef)
                .triggerId(this.triggerId)
                .workflowInstanceId(this.workflowInstanceId)
                .asyncTaskInstanceId(this.id)
                .workflowRef(this.workflowRef)
                .workflowVersion(this.workflowVersion)
                .nodeType(this.asyncTaskType)
                .build();
        this.raiseEvent(taskRetryEvent);
    }

    public void stop() {
        switch (this.failureMode) {
            case IGNORE:
                this.ignore();
                return;
            case SUSPEND:
                this.suspend();
                return;
            default:
                throw new RuntimeException("未知错误处理模式");
        }
    }

    public void doIgnore() {
        if (this.status != TaskStatus.SUSPENDED) {
            throw new RuntimeException("当前任务不能忽略");
        }
        this.ignore();
    }

    private void suspend() {
        this.status = TaskStatus.SUSPENDED;
        // 发布任务执行失败事件
        this.raiseEvent(
                TaskSuspendedEvent.Builder.aTaskSuspendedEvent()
                        .nodeRef(this.asyncTaskRef)
                        .triggerId(this.triggerId)
                        .workflowInstanceId(this.workflowInstanceId)
                        .asyncTaskInstanceId(this.id)
                        .workflowRef(this.workflowRef)
                        .workflowVersion(this.workflowVersion)
                        .nodeType(this.asyncTaskType)
                        .build()
        );
        // 发布状态变更事件
        this.publishStatusUpdatedEvent();
    }

    private void ignore() {
        this.status = TaskStatus.IGNORED;
        this.endTime = LocalDateTime.now();
        this.serialNo++;
        // 发布任务忽略事件
        this.raiseEvent(
                TaskIgnoredEvent.Builder.aTaskIgnoredEvent()
                        .nodeRef(this.asyncTaskRef)
                        .triggerId(this.triggerId)
                        .workflowInstanceId(this.workflowInstanceId)
                        .asyncTaskInstanceId(this.id)
                        .workflowRef(this.workflowRef)
                        .workflowVersion(this.workflowVersion)
                        .nodeType(this.asyncTaskType)
                        .build()
        );
        // 发布状态变更事件
        this.publishStatusUpdatedEvent();
    }

    public void succeed() {
        this.status = TaskStatus.SUCCEEDED;
        this.endTime = LocalDateTime.now();
        this.serialNo++;
        // 发布任务执行成功事件
        this.raiseEvent(
                TaskSucceededEvent.Builder.aTaskSucceededEvent()
                        .nodeRef(this.asyncTaskRef)
                        .triggerId(this.triggerId)
                        .workflowInstanceId(this.workflowInstanceId)
                        .asyncTaskInstanceId(this.id)
                        .workflowRef(this.workflowRef)
                        .workflowVersion(this.workflowVersion)
                        .nodeType(this.asyncTaskType)
                        .build()
        );
        // 发布状态变更事件
        this.publishStatusUpdatedEvent();
    }

    public void succeed(String nextTarget) {
        this.version++;
        this.nextTarget = nextTarget;
        this.endTime = LocalDateTime.now();
        this.serialNo++;
        this.status = TaskStatus.SUCCEEDED;
        // 发布任务执行成功事件
        this.raiseEvent(
                TaskSucceededEvent.Builder.aTaskSucceededEvent()
                        .nodeRef(this.asyncTaskRef)
                        .triggerId(this.triggerId)
                        .workflowInstanceId(this.workflowInstanceId)
                        .asyncTaskInstanceId(this.id)
                        .workflowRef(this.workflowRef)
                        .workflowVersion(this.workflowVersion)
                        .nodeType(this.asyncTaskType)
                        .build()
        );
        // 发布状态变更事件
        this.publishStatusUpdatedEvent();
    }

    public void fail() {
        this.status = TaskStatus.FAILED;
        this.endTime = LocalDateTime.now();
        // 发布任务执行失败事件
        this.raiseEvent(
                TaskFailedEvent.Builder.aTaskFailedEvent()
                        .nodeRef(this.asyncTaskRef)
                        .triggerId(this.triggerId)
                        .workflowInstanceId(this.workflowInstanceId)
                        .asyncTaskInstanceId(this.id)
                        .workflowRef(this.workflowRef)
                        .workflowVersion(this.workflowVersion)
                        .nodeType(this.asyncTaskType)
                        .build()
        );
        // 发布状态变更事件
        this.publishStatusUpdatedEvent();
    }

    public void skip() {
        this.version++;
        this.status = TaskStatus.SKIPPED;
        this.startTime = LocalDateTime.now();
        this.activatingTime = LocalDateTime.now();
        this.endTime = LocalDateTime.now();
        this.serialNo++;
        // 发布状态变更事件
        this.publishStatusUpdatedEvent();
    }

    // 中止任务执行
    public void terminate() {
        // 发布任务中止事件
        TaskTerminatingEvent terminatingEvent = TaskTerminatingEvent.Builder.aTaskTerminatingEvent()
                .nodeRef(this.asyncTaskRef)
                .triggerId(triggerId)
                .workflowInstanceId(this.workflowInstanceId)
                .asyncTaskInstanceId(this.id)
                .workflowRef(this.workflowRef)
                .workflowVersion(this.workflowVersion)
                .nodeType(this.asyncTaskType)
                .build();
        this.raiseEvent(terminatingEvent);
    }

    // 发布状态变更事件
    public void publishStatusUpdatedEvent() {
        this.raiseSseEvents(AsyncTaskInstanceStatusUpdatedEvent.builder()
                .workflowRef(this.workflowRef)
                .workflowVersion(this.workflowVersion)
                .workflowInstanceId(this.workflowInstanceId)
                .id(this.id)
                .status(this.status.name())
                .asyncTaskRef(this.asyncTaskRef)
                .asyncTaskType(this.asyncTaskType)
                .startTime(this.startTime == null ? null : this.startTime.toString())
                .endTime(this.endTime == null ? null : this.endTime.toString())
                .build());
    }

    public String getId() {
        return id;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public String getWorkflowRef() {
        return workflowRef;
    }

    public String getWorkflowVersion() {
        return workflowVersion;
    }

    public String getWorkflowInstanceId() {
        return workflowInstanceId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public FailureMode getFailureMode() {
        return failureMode;
    }

    public String getAsyncTaskRef() {
        return asyncTaskRef;
    }

    public String getAsyncTaskType() {
        return asyncTaskType;
    }

    public int getSerialNo() {
        return serialNo;
    }

    public String getNextTarget() {
        return nextTarget;
    }

    public LocalDateTime getActivatingTime() {
        return activatingTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public int getVersion() {
        return version;
    }

    public static final class Builder {
        // ID
        private final String id = UUID.randomUUID().toString().replace("-", "");
        // 触发器ID
        private String triggerId;
        // 流程定义唯一引用名称
        private String workflowRef;
        // 流程定义版本
        private String workflowVersion;
        // 流程定义版本
        private String workflowInstanceId;
        // 显示名称
        private String name;
        // 描述
        private String description;
        // 任务定义唯一引用名称
        private String asyncTaskRef;
        // 任务定义类型
        private String asyncTaskType;
        // 错误处理模式
        private FailureMode failureMode = FailureMode.SUSPEND;

        private Builder() {
        }

        public static Builder anAsyncTaskInstance() {
            return new Builder();
        }

        public Builder triggerId(String triggerId) {
            this.triggerId = triggerId;
            return this;
        }

        public Builder workflowRef(String workflowRef) {
            this.workflowRef = workflowRef;
            return this;
        }

        public Builder workflowVersion(String workflowVersion) {
            this.workflowVersion = workflowVersion;
            return this;
        }

        public Builder workflowInstanceId(String workflowInstanceId) {
            this.workflowInstanceId = workflowInstanceId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder asyncTaskRef(String asyncTaskRef) {
            this.asyncTaskRef = asyncTaskRef;
            return this;
        }

        public Builder asyncTaskType(String asyncTaskType) {
            this.asyncTaskType = asyncTaskType;
            return this;
        }

        public Builder failureMode(FailureMode failureMode) {
            this.failureMode = failureMode;
            return this;
        }

        public AsyncTaskInstance build() {
            AsyncTaskInstance asyncTaskInstance = new AsyncTaskInstance();
            asyncTaskInstance.id = this.id;
            asyncTaskInstance.description = this.description == null ? "" : this.description;
            asyncTaskInstance.triggerId = this.triggerId;
            asyncTaskInstance.workflowRef = this.workflowRef;
            asyncTaskInstance.workflowVersion = this.workflowVersion;
            asyncTaskInstance.workflowInstanceId = this.workflowInstanceId;
            asyncTaskInstance.name = this.name;
            asyncTaskInstance.asyncTaskRef = this.asyncTaskRef;
            asyncTaskInstance.asyncTaskType = this.asyncTaskType;
            asyncTaskInstance.failureMode = this.failureMode;
            // 执行次数计数，从0开始
            asyncTaskInstance.serialNo = 0;
            return asyncTaskInstance;
        }
    }
}
