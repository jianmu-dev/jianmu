package dev.jianmu.workflow.event.process;

/**
 * @author Ethan Liu
 * @class ProcessSuspendedEvent
 * @description ProcessSuspendedEvent
 * @create 2022-04-06 16:25
 */
public class ProcessSuspendedEvent extends ProcessEvent {
    private ProcessSuspendedEvent() {
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

        public static Builder aProcessSuspendedEvent() {
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

        public ProcessSuspendedEvent build() {
            ProcessSuspendedEvent processSuspendedEvent = new ProcessSuspendedEvent();
            processSuspendedEvent.workflowVersion = this.workflowVersion;
            processSuspendedEvent.workflowInstanceId = this.workflowInstanceId;
            processSuspendedEvent.workflowRef = this.workflowRef;
            processSuspendedEvent.triggerId = this.triggerId;
            return processSuspendedEvent;
        }
    }
}
