package dev.jianmu.workflow.event.definition;

/**
 * @author Ethan Liu
 * @class NodeSkipEvent
 * @description 节点跳过事件
 * @create 2021-10-18 11:28
 */
public class NodeSkipEvent extends DefinitionEvent {
    private NodeSkipEvent() {
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

        private Builder() {
        }

        public static Builder aNodeSkipEvent() {
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

        public NodeSkipEvent build() {
            NodeSkipEvent nodeSkipEvent = new NodeSkipEvent();
            nodeSkipEvent.triggerId = this.triggerId;
            nodeSkipEvent.nodeType = this.nodeType;
            nodeSkipEvent.workflowVersion = this.workflowVersion;
            nodeSkipEvent.nodeRef = this.nodeRef;
            nodeSkipEvent.workflowRef = this.workflowRef;
            return nodeSkipEvent;
        }
    }
}
