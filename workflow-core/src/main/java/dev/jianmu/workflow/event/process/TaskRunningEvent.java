package dev.jianmu.workflow.event.process;

/**
 * @author Ethan Liu
 * @program: workflow
 * @description 任务开始执行事件
 * @create 2021-01-23 20:09
 */
public class TaskRunningEvent extends AsyncTaskInstanceEvent {

    private TaskRunningEvent() {
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

        public static Builder aTaskRunningEvent() {
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

        public TaskRunningEvent build() {
            TaskRunningEvent taskRunningEvent = new TaskRunningEvent();
            taskRunningEvent.workflowInstanceId = this.workflowInstanceId;
            taskRunningEvent.asyncTaskInstanceId = this.asyncTaskInstanceId;
            taskRunningEvent.triggerId = this.triggerId;
            taskRunningEvent.workflowRef = this.workflowRef;
            taskRunningEvent.workflowVersion = this.workflowVersion;
            taskRunningEvent.nodeRef = this.nodeRef;
            taskRunningEvent.nodeType = this.nodeType;
            return taskRunningEvent;
        }
    }
}
