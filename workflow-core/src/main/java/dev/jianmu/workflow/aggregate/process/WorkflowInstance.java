package dev.jianmu.workflow.aggregate.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.jianmu.event.impl.WorkflowInstanceCreatedEvent;
import dev.jianmu.event.impl.WorkflowInstanceStatusUpdatedEvent;
import dev.jianmu.workflow.aggregate.AggregateRoot;
import dev.jianmu.workflow.aggregate.definition.GlobalParameter;
import dev.jianmu.workflow.event.process.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * @author Ethan Liu
 * @program: workflow
 * @description 流程运行实例
 * @create 2021-01-21 19:53
 */
public class WorkflowInstance extends AggregateRoot {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowInstance.class);

    // ID
    private String id;
    // 执行顺序号
    private int serialNo;
    // 触发器ID
    private String triggerId;
    // 触发器类型
    private String triggerType;
    // 显示名称
    private String name;
    // 描述
    private String description;
    // 运行模式
    private RunMode runMode = RunMode.AUTO;
    // 运行状态
    private ProcessStatus status = ProcessStatus.INIT;
    // 流程定义唯一引用名称
    private String workflowRef;
    // 流程定义版本
    private String workflowVersion;
    // 全局参数
    private Set<GlobalParameter> globalParameters = Set.of();
    // 创建时间
    private final LocalDateTime createTime = LocalDateTime.now();
    // 触发时间
    private LocalDateTime occurredTime;
    // 开始时间
    private LocalDateTime startTime;
    // 挂起时间
    private LocalDateTime suspendedTime;
    // 结束时间
    private LocalDateTime endTime;

    private WorkflowInstance() {
    }

    public boolean isRunning() {
        return (this.status == ProcessStatus.INIT || this.status == ProcessStatus.RUNNING || this.status == ProcessStatus.SUSPENDED);
    }

    public void statusCheck() {
        if (this.isRunning()) {
            return;
        }
        var processNotRunningEvent = ProcessNotRunningEvent.Builder.aProcessNotRunningEvent()
                .triggerId(triggerId)
                .workflowRef(this.workflowRef)
                .workflowVersion(this.workflowVersion)
                .build();
        this.raiseEvent(processNotRunningEvent);
    }

    // 初始化流程实例
    public void init(LocalDateTime occurredTime) {
        if (!this.isRunning()) {
            throw new RuntimeException("流程实例已终止或结束，无法初始化");
        }
        this.status = ProcessStatus.INIT;
        this.occurredTime = occurredTime;
        // 发布流程实例初始化运行事件
        var processStartedEvent = ProcessInitializedEvent.Builder.aProcessInitializedEvent()
                .triggerId(triggerId)
                .workflowRef(this.workflowRef)
                .workflowVersion(this.workflowVersion)
                .workflowInstanceId(this.id)
                .build();
        this.raiseEvent(processStartedEvent);
        // 发布流程实例创建事件
        this.raiseSseEvents(WorkflowInstanceCreatedEvent.builder()
                .id(this.id)
                .triggerId(this.triggerId)
                .triggerType(this.triggerType)
                .name(this.name)
                .description(this.description)
                .workflowRef(this.workflowRef)
                .workflowVersion(this.getWorkflowVersion())
                .serialNo(this.serialNo)
                .status(this.status.name())
                .build());
    }

    // 开始执行流程实例
    public void start() {
        if (!this.isRunning()) {
            throw new RuntimeException("流程实例已终止或结束，无法启动");
        }
        this.status = ProcessStatus.RUNNING;
        this.startTime = LocalDateTime.now();
        // 发布流程实例开始运行事件
        var processStartedEvent = ProcessStartedEvent.Builder.aProcessStartedEvent()
                .triggerId(triggerId)
                .workflowRef(this.workflowRef)
                .workflowVersion(this.workflowVersion)
                .workflowInstanceId(this.id)
                .build();
        this.raiseEvent(processStartedEvent);
        // 发布流程实例状态变更事件
        this.publishStatusUpdatedEvent();
    }

    // 挂起流程实例
    public void suspend() {
        if (this.status == ProcessStatus.TERMINATED) {
            var processTerminatedEvent = ProcessTerminatedEvent.Builder.aProcessTerminatedEvent()
                .triggerId(triggerId)
                .workflowRef(this.workflowRef)
                .workflowVersion(this.workflowVersion)
                .workflowInstanceId(this.id)
                .build();
            this.raiseEvent(processTerminatedEvent);
            logger.info("publish ProcessTerminatedEvent for task suspend: {}", processTerminatedEvent);
        }else {
            this.status = ProcessStatus.SUSPENDED;
            this.suspendedTime = LocalDateTime.now();
            var processSuspendedEvent = ProcessSuspendedEvent.Builder.aProcessSuspendedEvent()
                .triggerId(triggerId)
                .workflowRef(this.workflowRef)
                .workflowVersion(this.workflowVersion)
                .workflowInstanceId(this.id)
                .build();
            this.raiseEvent(processSuspendedEvent);
        }
        // 发布流程实例状态变更事件
        this.publishStatusUpdatedEvent();
    }

    // 恢复流程运行
    public void resume() {
        if (this.status != ProcessStatus.SUSPENDED) {
            throw new RuntimeException("流程实例未挂起，无法恢复");
        }
        this.status = ProcessStatus.RUNNING;
        var processRunningEvent = ProcessRunningEvent.Builder.aProcessRunningEvent()
                .triggerId(triggerId)
                .workflowRef(this.workflowRef)
                .workflowVersion(this.workflowVersion)
                .workflowInstanceId(this.id)
                .build();
        this.raiseEvent(processRunningEvent);
        // 发布流程实例状态变更事件
        this.publishStatusUpdatedEvent();
    }

    // 在start任务中终止流程实例
    public void terminateInStart() {
        this.status = ProcessStatus.TERMINATED;
        this.endTime = LocalDateTime.now();
        // 发布流程实例状态变更事件
        this.publishStatusUpdatedEvent();
    }

    // 终止流程实例
    public void terminate() {
        if (this.status == ProcessStatus.FINISHED) {
            throw new RuntimeException("流程实例已结束，无法终止");
        }
        this.status = ProcessStatus.TERMINATED;
        this.endTime = LocalDateTime.now();
        var processTerminatedEvent = ProcessTerminatedEvent.Builder.aProcessTerminatedEvent()
                .triggerId(triggerId)
                .workflowRef(this.workflowRef)
                .workflowVersion(this.workflowVersion)
                .workflowInstanceId(this.id)
                .build();
        this.raiseEvent(processTerminatedEvent);
        // 发布流程实例状态变更事件
        this.publishStatusUpdatedEvent();
    }

    // 结束流程实例
    public void end() {
        if (this.status == ProcessStatus.TERMINATED) {
            throw new RuntimeException("流程实例已终止，无法结束");
        }
        this.status = ProcessStatus.FINISHED;
        this.endTime = LocalDateTime.now();
        // 发布流程实例结束事件
        var processEndedEvent = ProcessEndedEvent.Builder.aProcessEndedEvent()
                .triggerId(triggerId)
                .workflowRef(this.workflowRef)
                .workflowVersion(this.workflowVersion)
                .workflowInstanceId(this.id)
                .build();
        this.raiseEvent(processEndedEvent);
        // 发布流程实例状态变更事件
        this.publishStatusUpdatedEvent();
    }

    // 发布流程实例状态变更事件
    public void publishStatusUpdatedEvent() {
        var event = WorkflowInstanceStatusUpdatedEvent.builder()
                .workflowRef(this.workflowRef)
                .workflowVersion(this.workflowVersion)
                .id(this.id)
                .status(this.status.name())
                .startTime(this.startTime == null ? null : this.startTime.toString())
                .suspendedTime(this.suspendedTime == null ? null : this.suspendedTime.toString())
                .endTime(this.endTime == null ? null : this.endTime.toString())
                .build();
        this.raiseSseEvents(event);
    }

    public String getId() {
        return id;
    }

    public int getSerialNo() {
        return serialNo;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public RunMode getRunMode() {
        return runMode;
    }

    public ProcessStatus getStatus() {
        return status;
    }

    public String getWorkflowRef() {
        return workflowRef;
    }

    public String getWorkflowVersion() {
        return workflowVersion;
    }

    public Set<GlobalParameter> getGlobalParameters() {
        return globalParameters;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getSuspendedTime() {
        return suspendedTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public static final class Builder {
        // ID
        // TODO 暂时使用UUID的值
        private String id = UUID.randomUUID().toString().replace("-", "");
        // 执行顺序号
        private int serialNo;
        // 触发器ID
        private String triggerId;
        // 触发器类型
        private String triggerType;
        // 显示名称
        private String name;
        // 描述
        private String description;
        // 流程定义唯一引用名称
        private String workflowRef;
        // 流程定义版本
        private String workflowVersion;
        // 全局参数
        private Set<GlobalParameter> globalParameters;

        private Builder() {
        }

        public static Builder aWorkflowInstance() {
            return new Builder();
        }

        public Builder serialNo(int serialNo) {
            this.serialNo = serialNo;
            return this;
        }

        public Builder triggerId(String triggerId) {
            this.triggerId = triggerId;
            return this;
        }

        public Builder triggerType(String triggerType) {
            this.triggerType = triggerType;
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

        public Builder workflowRef(String workflowRef) {
            this.workflowRef = workflowRef;
            return this;
        }

        public Builder workflowVersion(String workflowVersion) {
            this.workflowVersion = workflowVersion;
            return this;
        }

        public Builder globalParameters(Set<GlobalParameter> globalParameters) {
            this.globalParameters = globalParameters;
            return this;
        }

        public WorkflowInstance build() {
            WorkflowInstance workflowInstance = new WorkflowInstance();
            workflowInstance.workflowVersion = this.workflowVersion;
            workflowInstance.serialNo = this.serialNo;
            workflowInstance.triggerId = this.triggerId;
            workflowInstance.triggerType = this.triggerType;
            workflowInstance.name = this.name;
            workflowInstance.description = this.description;
            workflowInstance.id = this.id;
            workflowInstance.workflowRef = this.workflowRef;
            workflowInstance.globalParameters = this.globalParameters;
            return workflowInstance;
        }
    }
}
