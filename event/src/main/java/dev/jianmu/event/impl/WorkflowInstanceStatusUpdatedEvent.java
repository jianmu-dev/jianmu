package dev.jianmu.event.impl;

/**
 * @class WorkflowInstanceStatusUpdatedEvent
 * @description 流程实例状态更新事件
 * @author Daihw
 * @create 2022/9/15 1:49 下午
 */
public class WorkflowInstanceStatusUpdatedEvent extends BaseEvent {
    private String id;
    private String status;
    private String workflowRef;
    private String workflowVersion;

    public String getWorkflowRef() {
        return workflowRef;
    }

    public String getWorkflowVersion() {
        return workflowVersion;
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public static class Builder{
        private String id;
        private String status;
        private String workflowRef;
        private String workflowVersion;

        public static Builder aWorkflowInstanceStatusUpdatedEvent() {
            return new Builder();
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder workflowRef(String workflowRef) {
            this.workflowRef = workflowRef;
            return this;
        }

        public Builder workflowVersion(String workflowVersion) {
            this.workflowVersion = workflowVersion;
            return this;
        }

        public WorkflowInstanceStatusUpdatedEvent build() {
            var event = new WorkflowInstanceStatusUpdatedEvent();
            event.id = this.id;
            event.status = this.status;
            event.workflowRef = this.workflowRef;
            event.workflowVersion = this.workflowVersion;
            return event;
        }
    }

    @Override
    public String toString() {
        return "WorkflowInstanceStatusUpdatedEvent{" +
                "id='" + id + '\'' +
                ", status='" + status + '\'' +
                ", workflowRef='" + workflowRef + '\'' +
                ", workflowVersion='" + workflowVersion + '\'' +
                '}';
    }
}
