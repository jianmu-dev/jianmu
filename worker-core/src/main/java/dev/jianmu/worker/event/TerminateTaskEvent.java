package dev.jianmu.worker.event;

/**
 * @class TerminateTaskEvent
 * @description TerminateTaskEvent
 * @author Ethan Liu
 * @create 2021-11-12 13:43
 */
public class TerminateTaskEvent {
    private String workerId;
    private String workerType;
    private String taskInstanceId;

    public String getWorkerId() {
        return workerId;
    }

    public String getWorkerType() {
        return workerType;
    }

    public String getTaskInstanceId() {
        return taskInstanceId;
    }

    public static final class Builder {
        private String workerId;
        private String workerType;
        private String taskInstanceId;

        private Builder() {
        }

        public static Builder aTerminateTaskEvent() {
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

        public Builder taskInstanceId(String taskInstanceId) {
            this.taskInstanceId = taskInstanceId;
            return this;
        }

        public TerminateTaskEvent build() {
            TerminateTaskEvent terminateTaskEvent = new TerminateTaskEvent();
            terminateTaskEvent.workerId = this.workerId;
            terminateTaskEvent.taskInstanceId = this.taskInstanceId;
            terminateTaskEvent.workerType = this.workerType;
            return terminateTaskEvent;
        }
    }
}
