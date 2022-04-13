package dev.jianmu.workflow.event.process;

/**
 * @author Ethan Liu
 * @class TaskIgnoredEvent
 * @description TaskIgnoredEvent
 * @create 2022-04-09 11:58
 */
public class TaskIgnoredEvent extends AsyncTaskInstanceEvent {
    private TaskIgnoredEvent() {
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
        // 异步任务实例ID
        protected String asyncTaskInstanceId;
        // 节点唯一引用名称
        protected String nodeRef;
        // 节点类型
        protected String nodeType;

        private Builder() {
        }

        public static Builder aTaskIgnoredEvent() {
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

        public Builder asyncTaskInstanceId(String asyncTaskInstanceId) {
            this.asyncTaskInstanceId = asyncTaskInstanceId;
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

        public TaskIgnoredEvent build() {
            TaskIgnoredEvent taskIgnoredEvent = new TaskIgnoredEvent();
            taskIgnoredEvent.asyncTaskInstanceId = this.asyncTaskInstanceId;
            taskIgnoredEvent.workflowRef = this.workflowRef;
            taskIgnoredEvent.workflowVersion = this.workflowVersion;
            taskIgnoredEvent.workflowInstanceId = this.workflowInstanceId;
            taskIgnoredEvent.triggerId = this.triggerId;
            taskIgnoredEvent.nodeRef = this.nodeRef;
            taskIgnoredEvent.nodeType = this.nodeType;
            return taskIgnoredEvent;
        }
    }
}
