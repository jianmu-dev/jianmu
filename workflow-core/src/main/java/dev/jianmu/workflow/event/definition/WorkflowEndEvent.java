package dev.jianmu.workflow.event.definition;

/**
 * @author Ethan Liu
 * @class WorkflowEndEvent
 * @description 流程结束事件
 * @create 2021-03-19 08:43
 */
public class WorkflowEndEvent extends DefinitionEvent {
    private WorkflowEndEvent() {
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

        private Builder() {
        }

        public static Builder aWorkflowEndEvent() {
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

        public WorkflowEndEvent build() {
            WorkflowEndEvent workflowEndEvent = new WorkflowEndEvent();
            workflowEndEvent.workflowRef = this.workflowRef;
            workflowEndEvent.nodeRef = this.nodeRef;
            workflowEndEvent.triggerId = this.triggerId;
            workflowEndEvent.workflowVersion = this.workflowVersion;
            return workflowEndEvent;
        }
    }
}
