package dev.jianmu.workflow.aggregate.process;

import dev.jianmu.workflow.aggregate.AggregateRoot;
import dev.jianmu.workflow.event.process.ProcessEndedEvent;
import dev.jianmu.workflow.event.process.ProcessStartedEvent;
import dev.jianmu.workflow.event.process.ProcessTerminatedEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author Ethan Liu
 * @program: workflow
 * @description 流程运行实例
 * @create 2021-01-21 19:53
 */
public class WorkflowInstance extends AggregateRoot {
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
    private ProcessStatus status = ProcessStatus.RUNNING;
    // 流程定义唯一引用名称
    private String workflowRef;
    // 流程定义版本
    private String workflowVersion;
    // 创建时间
    private final LocalDateTime createTime = LocalDateTime.now();
    // 开始时间
    private LocalDateTime startTime;
    // 结束时间
    private LocalDateTime endTime;

    private WorkflowInstance() {
    }

    public boolean isRunning() {
        return this.status == ProcessStatus.RUNNING;
    }

    // 启动流程实例
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
                .build();
        this.raiseEvent(processStartedEvent);
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
                .build();
        this.raiseEvent(processEndedEvent);
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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
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
            return workflowInstance;
        }
    }
}
