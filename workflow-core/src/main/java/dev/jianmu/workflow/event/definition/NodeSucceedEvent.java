package dev.jianmu.workflow.event.definition;

/**
 * @author Ethan Liu
 * @class NodeSucceedEvent
 * @description NodeSucceedEvent
 * @create 2022-03-18 17:30
 */
public class NodeSucceedEvent extends DefinitionEvent {
    private NodeSucceedEvent() {
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

        public static Builder aNodeSucceedEvent() {
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

        public NodeSucceedEvent build() {
            NodeSucceedEvent nodeSucceedEvent = new NodeSucceedEvent();
            nodeSucceedEvent.workflowRef = this.workflowRef;
            nodeSucceedEvent.workflowVersion = this.workflowVersion;
            nodeSucceedEvent.nodeType = this.nodeType;
            nodeSucceedEvent.nodeRef = this.nodeRef;
            nodeSucceedEvent.triggerId = this.triggerId;
            return nodeSucceedEvent;
        }
    }
}
