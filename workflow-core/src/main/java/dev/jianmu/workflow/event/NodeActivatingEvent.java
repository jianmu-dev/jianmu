package dev.jianmu.workflow.event;

/**
 * @class NodeActivatingEvent
 * @description 节点激活事件
 * @author Ethan Liu
 * @create 2021-03-17 13:53
*/
public class NodeActivatingEvent extends BaseEvent {
    private NodeActivatingEvent() {
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

        public NodeActivatingEvent build() {
            NodeActivatingEvent nodeActivatingEvent = new NodeActivatingEvent();
            nodeActivatingEvent.triggerId = this.triggerId;
            nodeActivatingEvent.workflowRef = this.workflowRef;
            nodeActivatingEvent.workflowVersion = this.workflowVersion;
            nodeActivatingEvent.workflowInstanceId = this.workflowInstanceId;
            nodeActivatingEvent.nodeRef = this.nodeRef;
            nodeActivatingEvent.nodeType = this.nodeType;
            return nodeActivatingEvent;
        }
    }
}
