package dev.jianmu.workflow.event.process;

/**
 * @class ProcessVolumeCreatedEvent
 * @description 流程创建volume事件
 * @author Daihw
 * @create 2022/6/14 5:55 下午
 */
public class ProcessVolumeCreatedEvent extends ProcessEvent {
    private ProcessVolumeCreatedEvent() {
    }

    public static final class Builder {
        // 流程定义唯一引用名称
        protected String workflowRef;
        // 流程定义版本
        protected String workflowVersion;
        // 流程实例ID
        protected String workflowInstanceId;
        // 触发器ID
        protected String triggerId;

        private Builder() {
        }

        public static Builder aProcessVolumeCreatedEvent() {
            return new Builder();
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

        public Builder triggerId(String triggerId) {
            this.triggerId = triggerId;
            return this;
        }

        public ProcessVolumeCreatedEvent build() {
            ProcessVolumeCreatedEvent processStartedEvent = new ProcessVolumeCreatedEvent();
            processStartedEvent.workflowInstanceId = this.workflowInstanceId;
            processStartedEvent.triggerId = this.triggerId;
            processStartedEvent.workflowRef = this.workflowRef;
            processStartedEvent.workflowVersion = this.workflowVersion;
            return processStartedEvent;
        }
    }
}
