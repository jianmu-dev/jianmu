package dev.jianmu.trigger.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * @class: TriggerEvent
 * @description: TriggerEvent
 * @author: Ethan Liu
 * @create: 2021-05-25 08:25
 **/
public class TriggerEvent {
    private String id;
    private String projectId;
    private String triggerId;
    private String triggerType;
    // Payload
    private String payload;
    // 触发时间
    private LocalDateTime occurredTime;
    private List<TriggerEventParameter> parameters;

    public void setParameters(List<TriggerEventParameter> parameters) {
        this.parameters = parameters;
    }

    public String getId() {
        return id;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public String getPayload() {
        return payload;
    }

    public LocalDateTime getOccurredTime() {
        return occurredTime;
    }

    public List<TriggerEventParameter> getParameters() {
        if (parameters == null) {
            return List.of();
        }
        return parameters;
    }

    public static final class Builder {
        private String projectId;
        private String triggerId;
        private String triggerType;
        // Payload
        private String payload;
        private List<TriggerEventParameter> parameters;

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

        public Builder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public Builder parameters(List<TriggerEventParameter> parameters) {
            this.parameters = parameters;
            return this;
        }

        public TriggerEvent build() {
            TriggerEvent triggerEvent = new TriggerEvent();
            triggerEvent.id = UUID.randomUUID().toString().replace("-", "");
            triggerEvent.occurredTime = LocalDateTime.now();
            triggerEvent.parameters = this.parameters;
            triggerEvent.projectId = this.projectId;
            triggerEvent.triggerId = this.triggerId;
            triggerEvent.triggerType = this.triggerType;
            triggerEvent.payload = this.payload;
            return triggerEvent;
        }
    }

    @Override
    public String toString() {
        return "TriggerEvent{" +
                "id='" + id + '\'' +
                ", projectId='" + projectId + '\'' +
                ", triggerId='" + triggerId + '\'' +
                ", triggerType='" + triggerType + '\'' +
                ", occurredTime=" + occurredTime +
                ", parameters=" + parameters +
                '}';
    }
}
