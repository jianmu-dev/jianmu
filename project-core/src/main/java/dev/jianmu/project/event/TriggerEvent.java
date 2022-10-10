package dev.jianmu.project.event;

import java.time.LocalDateTime;

/**
 * @class TriggerEvent
 * @description TriggerEvent
 * @author Ethan Liu
 * @create 2021-08-17 22:14
*/
public class TriggerEvent {
    private String projectId;
    private String triggerId;
    private String triggerType;
    private String workflowRef;
    private String workflowVersion;
    private LocalDateTime occurredTime;

    public String getProjectId() {
        return projectId;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public String getWorkflowRef() {
        return workflowRef;
    }

    public String getWorkflowVersion() {
        return workflowVersion;
    }

    public LocalDateTime getOccurredTime() {
        return occurredTime;
    }

    public static final class Builder {
        private String projectId;
        private String triggerId;
        private String triggerType;
        private String workflowRef;
        private String workflowVersion;
        private LocalDateTime occurredTime;

        private Builder() {
        }

        public static Builder aTriggerEvent() {
            return new Builder();
        }

        public Builder projectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder triggerId(String triggerId) {
            this.triggerId = triggerId;
            return this;
        }

        public Builder triggerType(String triggerType) {
            this.triggerType = triggerType;
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

        public Builder occurredTime(LocalDateTime occurredTime) {
            this.occurredTime = occurredTime;
            return this;
        }

        public TriggerEvent build() {
            TriggerEvent triggerEvent = new TriggerEvent();
            triggerEvent.workflowVersion = this.workflowVersion;
            triggerEvent.workflowRef = this.workflowRef;
            triggerEvent.projectId = this.projectId;
            triggerEvent.triggerId = this.triggerId;
            triggerEvent.triggerType = this.triggerType;
            triggerEvent.occurredTime = this.occurredTime;
            return triggerEvent;
        }
    }
}
