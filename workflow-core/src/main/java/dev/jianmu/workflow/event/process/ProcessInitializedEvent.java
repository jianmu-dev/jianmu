package dev.jianmu.workflow.event.process;

/**
 * @author Ethan Liu
 * @class ProcessRunningEvent
 * @description 流程实例初始化运行事件
 * @create 2022-01-02 10:26
 */
public class ProcessInitializedEvent extends ProcessEvent {
    private ProcessInitializedEvent() {
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

        public static Builder aProcessInitializedEvent() {
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

        public ProcessInitializedEvent build() {
            ProcessInitializedEvent processStartedEvent = new ProcessInitializedEvent();
            processStartedEvent.workflowInstanceId = this.workflowInstanceId;
            processStartedEvent.triggerId = this.triggerId;
            processStartedEvent.workflowRef = this.workflowRef;
            processStartedEvent.workflowVersion = this.workflowVersion;
            return processStartedEvent;
        }
    }
}
