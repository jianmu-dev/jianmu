package dev.jianmu.worker.event;

/**
 * @class CreateWorkspaceEvent
 * @description CreateWorkspaceEvent
 * @author Ethan Liu
 * @create 2021-09-14 09:02
*/
public class CreateWorkspaceEvent {
    private String workerId;
    private String workerType;
    private String workspaceName;
    // 流程定义唯一引用名称
    private String workflowRef;
    // 流程定义版本
    private String workflowVersion;
    // 流程实例ID
    private String workflowInstanceId;
    // 触发器ID
    private String triggerId;

    public String getWorkerId() {
        return workerId;
    }

    public String getWorkerType() {
        return workerType;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public String getWorkflowRef() {
        return workflowRef;
    }

    public String getWorkflowVersion() {
        return workflowVersion;
    }

    public String getWorkflowInstanceId() {
        return workflowInstanceId;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public static final class Builder {
        private String workerId;
        private String workerType;
        private String workspaceName;
        // 流程定义唯一引用名称
        private String workflowRef;
        // 流程定义版本
        private String workflowVersion;
        // 流程实例ID
        private String workflowInstanceId;
        // 触发器ID
        private String triggerId;

        private Builder() {
        }

        public static Builder aCreateWorkspaceEvent() {
            return new Builder();
        }

        public Builder workerId(String workerId) {
            this.workerId = workerId;
            return this;
        }

        public Builder workerType(String workerType) {
            this.workerType = workerType;
            return this;
        }

        public Builder workspaceName(String workspaceName) {
            this.workspaceName = workspaceName;
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

        public Builder workflowInstanceId(String workflowInstanceId) {
            this.workflowInstanceId = workflowInstanceId;
            return this;
        }

        public Builder triggerId(String triggerId) {
            this.triggerId = triggerId;
            return this;
        }

        public CreateWorkspaceEvent build() {
            CreateWorkspaceEvent createWorkspaceEvent = new CreateWorkspaceEvent();
            createWorkspaceEvent.workflowVersion = this.workflowVersion;
            createWorkspaceEvent.workerId = this.workerId;
            createWorkspaceEvent.workflowRef = this.workflowRef;
            createWorkspaceEvent.workflowInstanceId = this.workflowInstanceId;
            createWorkspaceEvent.triggerId = this.triggerId;
            createWorkspaceEvent.workerType = this.workerType;
            createWorkspaceEvent.workspaceName = this.workspaceName;
            return createWorkspaceEvent;
        }
    }
}
