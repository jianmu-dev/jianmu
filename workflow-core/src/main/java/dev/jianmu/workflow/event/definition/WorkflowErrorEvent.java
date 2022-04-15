package dev.jianmu.workflow.event.definition;

/**
 * @author Ethan Liu
 * @class WorkflowErrorEvent
 * @description WorkflowErrorEvent
 * @create 2022-04-15 12:24
 */
public class WorkflowErrorEvent extends DefinitionEvent {
    private WorkflowErrorEvent() {
    }

    public static final class Builder {
        // 流程定义唯一引用名称
        protected String workflowRef;
        // 流程定义版本
        protected String workflowVersion;
        // 触发器ID
        protected String triggerId;
        // 节点唯一引用名称
        protected String nodeRef;
        // 节点类型
        protected String nodeType;
        // 事件发送者
        protected String sender;

        private Builder() {
        }

        public static Builder aWorkflowErrorEvent() {
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

        public Builder sender(String sender) {
            this.sender = sender;
            return this;
        }

        public WorkflowErrorEvent build() {
            WorkflowErrorEvent workflowErrorEvent = new WorkflowErrorEvent();
            workflowErrorEvent.triggerId = this.triggerId;
            workflowErrorEvent.nodeRef = this.nodeRef;
            workflowErrorEvent.nodeType = this.nodeType;
            workflowErrorEvent.sender = this.sender;
            workflowErrorEvent.workflowRef = this.workflowRef;
            workflowErrorEvent.workflowVersion = this.workflowVersion;
            return workflowErrorEvent;
        }
    }
}
