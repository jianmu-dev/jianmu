package dev.jianmu.workflow.event.definition;

/**
 * @author Ethan Liu
 * @class WorkflowStartEvent
 * @description 流程启动事件
 * @create 2021-03-19 08:36
 */
public class WorkflowStartEvent extends DefinitionEvent {
    private WorkflowStartEvent() {
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

        public static Builder aWorkflowStartEvent() {
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

        public WorkflowStartEvent build() {
            WorkflowStartEvent workflowStartEvent = new WorkflowStartEvent();
            workflowStartEvent.workflowRef = this.workflowRef;
            workflowStartEvent.nodeRef = this.nodeRef;
            workflowStartEvent.triggerId = this.triggerId;
            workflowStartEvent.workflowVersion = this.workflowVersion;
            return workflowStartEvent;
        }
    }
}
