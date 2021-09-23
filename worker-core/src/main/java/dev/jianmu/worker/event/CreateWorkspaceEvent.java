package dev.jianmu.worker.event;

/**
 * @class: CreateWorkspaceEvent
 * @description: CreateWorkspaceEvent
 * @author: Ethan Liu
 * @create: 2021-09-14 09:02
 **/
public class CreateWorkspaceEvent {
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

        public CreateWorkspaceEvent build() {
            CreateWorkspaceEvent createWorkspaceEvent = new CreateWorkspaceEvent();
            createWorkspaceEvent.workerId = this.workerId;
            createWorkspaceEvent.workspaceName = this.workspaceName;
            createWorkspaceEvent.workerType = this.workerType;
            return createWorkspaceEvent;
        }
    }
}
