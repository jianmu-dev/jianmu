package dev.jianmu.worker.event;

/**
 * @class: CleanupWorkspaceEvent
 * @description: CleanupWorkspaceEvent
 * @author: Ethan Liu
 * @create: 2021-09-14 09:07
 **/
public class CleanupWorkspaceEvent {
    private String workerId;
    private String workerType;
    private String workspaceName;

    public String getWorkerId() {
        return workerId;
    }

    public String getWorkerType() {
        return workerType;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public static final class Builder {
        private String workerId;
        private String workerType;
        private String workspaceName;

        private Builder() {
        }

        public static Builder aCleanupWorkspaceEvent() {
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

        public CleanupWorkspaceEvent build() {
            CleanupWorkspaceEvent cleanupWorkspaceEvent = new CleanupWorkspaceEvent();
            cleanupWorkspaceEvent.workerId = this.workerId;
            cleanupWorkspaceEvent.workerType = this.workerType;
            cleanupWorkspaceEvent.workspaceName = this.workspaceName;
            return cleanupWorkspaceEvent;
        }
    }
}
