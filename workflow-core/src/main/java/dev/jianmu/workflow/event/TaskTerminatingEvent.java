package dev.jianmu.workflow.event;

/**
 * @program: workflow
 * @description: 任务中止事件
 * @author: Ethan Liu
 * @create: 2021-01-21 20:48
 **/
public class TaskTerminatingEvent extends BaseEvent {
    private TaskTerminatingEvent() {
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

        public static Builder aTaskTerminatingEvent() {
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

        public TaskTerminatingEvent build() {
            TaskTerminatingEvent taskTerminatingEvent = new TaskTerminatingEvent();
            taskTerminatingEvent.triggerId = this.triggerId;
            taskTerminatingEvent.workflowRef = this.workflowRef;
            taskTerminatingEvent.workflowVersion = this.workflowVersion;
            taskTerminatingEvent.workflowInstanceId = this.workflowInstanceId;
            taskTerminatingEvent.nodeRef = this.nodeRef;
            taskTerminatingEvent.nodeType = this.nodeType;
            return taskTerminatingEvent;
        }
    }
}
