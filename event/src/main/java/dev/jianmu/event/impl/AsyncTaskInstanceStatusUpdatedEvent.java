package dev.jianmu.event.impl;

/**
 * @class AsyncTaskInstanceStatusUpdatedEvent
 * @description 异步任务实例状态更新事件
 * @author Daihw
 * @create 2022/9/15 1:52 下午
 */
public class AsyncTaskInstanceStatusUpdatedEvent extends BaseEvent{
    private String workflowRef;
    private String workflowVersion;
    private String workflowInstanceId;
    private String id;
    private String status;
    private String asyncTaskRef;
    private String asyncTaskType;

    public String getWorkflowRef() {
        return workflowRef;
    }

    public String getWorkflowVersion() {
        return workflowVersion;
    }

    public String getAsyncTaskRef() {
        return asyncTaskRef;
    }

    public String getAsyncTaskType() {
        return asyncTaskType;
    }

    public String getWorkflowInstanceId() {
        return workflowInstanceId;
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public static class Builder{
        private String workflowRef;
        private String workflowVersion;
        private String workflowInstanceId;
        private String id;
        private String status;
        private String asyncTaskRef;
        private String asyncTaskType;

        public static Builder aWorkflowInstanceStatusUpdatedEvent() {
            return new Builder();
        }

        public Builder workflowInstanceId(String workflowInstanceId) {
            this.workflowInstanceId = workflowInstanceId;
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

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder asyncTaskRef(String asyncTaskRef) {
            this.asyncTaskRef = asyncTaskRef;
            return this;
        }

        public Builder asyncTaskType(String asyncTaskType) {
            this.asyncTaskType = asyncTaskType;
            return this;
        }

        public AsyncTaskInstanceStatusUpdatedEvent build() {
            var event = new AsyncTaskInstanceStatusUpdatedEvent();
            event.workflowRef = this.workflowRef;
            event.workflowVersion = this.workflowVersion;
            event.workflowInstanceId = this.workflowInstanceId;
            event.id = this.id;
            event.status = this.status;
            event.asyncTaskRef = this.asyncTaskRef;
            event.asyncTaskType = this.asyncTaskType;
            return event;
        }
    }

    @Override
    public String toString() {
        return "AsyncTaskInstanceStatusUpdatedEvent{" +
                "workflowRef='" + workflowRef + '\'' +
                ", workflowVersion='" + workflowVersion + '\'' +
                ", workflowInstanceId='" + workflowInstanceId + '\'' +
                ", id='" + id + '\'' +
                ", status='" + status + '\'' +
                ", asyncTaskRef='" + asyncTaskRef + '\'' +
                ", asyncTaskType='" + asyncTaskType + '\'' +
                '}';
    }
}
