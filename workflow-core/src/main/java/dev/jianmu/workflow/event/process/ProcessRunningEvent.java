package dev.jianmu.workflow.event.process;

/**
 * @author Ethan Liu
 * @class ProcessRunningEvent
 * @description TODO
 * @create 2022-04-06 16:57
 */
public class ProcessRunningEvent extends ProcessEvent {
    private ProcessRunningEvent() {
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

        public static Builder aProcessRunningEvent() {
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

        public ProcessRunningEvent build() {
            ProcessRunningEvent processRunningEvent = new ProcessRunningEvent();
            processRunningEvent.workflowVersion = this.workflowVersion;
            processRunningEvent.workflowInstanceId = this.workflowInstanceId;
            processRunningEvent.nodeType = this.nodeType;
            processRunningEvent.workflowRef = this.workflowRef;
            processRunningEvent.triggerId = this.triggerId;
            processRunningEvent.nodeRef = this.nodeRef;
            return processRunningEvent;
        }
    }
}
