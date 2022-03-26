package dev.jianmu.workflow.event.definition;

/**
 * @author Ethan Liu
 * @class NodeActivatingEvent
 * @description 节点激活事件
 * @create 2021-03-17 13:53
 */
public class NodeActivatingEvent extends DefinitionEvent {
    private NodeActivatingEvent() {
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

        public static Builder aNodeActivatingEvent() {
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

        public NodeActivatingEvent build() {
            NodeActivatingEvent nodeActivatingEvent = new NodeActivatingEvent();
            nodeActivatingEvent.triggerId = this.triggerId;
            nodeActivatingEvent.workflowRef = this.workflowRef;
            nodeActivatingEvent.workflowVersion = this.workflowVersion;
            nodeActivatingEvent.nodeRef = this.nodeRef;
            nodeActivatingEvent.nodeType = this.nodeType;
            nodeActivatingEvent.sender = this.sender;
            return nodeActivatingEvent;
        }
    }
}
