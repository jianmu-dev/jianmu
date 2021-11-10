package dev.jianmu.trigger.event;

/**
 * @class: TriggerEvent
 * @description: TriggerEvent
 * @author: Ethan Liu
 * @create: 2021-05-25 08:25
 **/
public class TriggerEvent {
    private String triggerId;
    private String projectId;
    private String type;

    public String getTriggerId() {
        return triggerId;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getType() {
        return type;
    }

    public static final class Builder {
        private String triggerId;
        private String projectId;
        private String type;

        private Builder() {
        }

        public static Builder aTriggerEvent() {
            return new Builder();
        }

        public Builder triggerId(String triggerId) {
            this.triggerId = triggerId;
            return this;
        }

        public Builder projectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public TriggerEvent build() {
            TriggerEvent triggerEvent = new TriggerEvent();
            triggerEvent.projectId = this.projectId;
            triggerEvent.triggerId = this.triggerId;
            triggerEvent.type = this.type;
            return triggerEvent;
        }
    }

    @Override
    public String toString() {
        return "TriggerEvent{" +
                "triggerId='" + triggerId + '\'' +
                ", projectId='" + projectId + '\'' +
                '}';
    }
}
