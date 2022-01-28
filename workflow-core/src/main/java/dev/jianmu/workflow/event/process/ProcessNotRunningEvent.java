package dev.jianmu.workflow.event.process;

/**
 * @author Ethan Liu
 * @class ProcessNotRunningEvent
 * @description ProcessNotRunningEvent
 * @create 2022-01-28 15:34
 */
public class ProcessNotRunningEvent extends ProcessEvent {
    private ProcessNotRunningEvent() {
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
        // 节点唯一引用名称
        protected String nodeRef;
        // 节点类型
        protected String nodeType;

        private Builder() {
        }

        public static Builder aProcessNotRunningEvent() {
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

        public Builder nodeRef(String nodeRef) {
            this.nodeRef = nodeRef;
            return this;
        }

        public Builder nodeType(String nodeType) {
            this.nodeType = nodeType;
            return this;
        }

        public ProcessNotRunningEvent build() {
            ProcessNotRunningEvent processNotRunningEvent = new ProcessNotRunningEvent();
            processNotRunningEvent.workflowRef = this.workflowRef;
            processNotRunningEvent.workflowVersion = this.workflowVersion;
            processNotRunningEvent.workflowInstanceId = this.workflowInstanceId;
            processNotRunningEvent.nodeType = this.nodeType;
            processNotRunningEvent.triggerId = this.triggerId;
            processNotRunningEvent.nodeRef = this.nodeRef;
            return processNotRunningEvent;
        }
    }
}
