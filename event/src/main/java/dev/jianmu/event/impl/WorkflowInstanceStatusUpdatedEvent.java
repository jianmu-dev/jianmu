package dev.jianmu.event.impl;

/**
 * @class WorkflowInstanceStatusUpdatedEvent
 * @description 流程实例状态更新事件
 * @author Daihw
 * @create 2022/9/15 1:49 下午
 */
public class WorkflowInstanceStatusUpdatedEvent extends BaseEvent {
    private String workflowInstanceId;
    private String workflowInstanceStatus;
    private String workflowRef;
    private String workflowVersion;

    public String getWorkflowRef() {
        return workflowRef;
    }

    public String getWorkflowVersion() {
        return workflowVersion;
    }

    public String getWorkflowInstanceId() {
        return workflowInstanceId;
    }

    public String getWorkflowInstanceStatus() {
        return workflowInstanceStatus;
    }

    public static class Builder{
        private String workflowInstanceId;
        private String workflowInstanceStatus;
        private String workflowRef;
        private String workflowVersion;

        public static Builder aWorkflowInstanceStatusUpdatedEvent() {
            return new Builder();
        }

        public Builder workflowInstanceId(String workflowInstanceId) {
            this.workflowInstanceId = workflowInstanceId;
            return this;
        }

        public Builder workflowInstanceStatus(String workflowInstanceStatus) {
            this.workflowInstanceStatus = workflowInstanceStatus;
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
            event.workflowInstanceId = this.workflowInstanceId;
            event.workflowInstanceStatus = this.workflowInstanceStatus;
            event.workflowRef = this.workflowRef;
            event.workflowVersion = this.workflowVersion;
            return event;
        }
    }

    @Override
    public String toString() {
        return "WorkflowInstanceStatusUpdatedEvent{" +
                "workflowInstanceId='" + workflowInstanceId + '\'' +
                ", workflowInstanceStatus='" + workflowInstanceStatus + '\'' +
                ", workflowRef='" + workflowRef + '\'' +
                ", workflowVersion='" + workflowVersion + '\'' +
                '}';
    }
}
