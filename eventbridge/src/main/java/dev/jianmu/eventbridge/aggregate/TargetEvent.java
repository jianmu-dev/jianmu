package dev.jianmu.eventbridge.aggregate;

import java.util.Set;
import java.util.UUID;

/**
 * @class: Event
 * @description: 事件
 * @author: Ethan Liu
 * @create: 2021-08-10 11:43
 **/
public class TargetEvent {
    private String id;
    private String sourceId;
    private String targetId;
    private String destinationId;
    private Payload payload;
    private Set<EventParameter> eventParameters;

    public String getId() {
        return id;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public Payload getPayload() {
        return payload;
    }

    public Set<EventParameter> getEventParameters() {
        return eventParameters;
    }

    public void setEventParameters(Set<EventParameter> eventParameters) {
        this.eventParameters = eventParameters;
    }

    public static final class Builder {
        private String sourceId;
        private String targetId;
        private String destinationId;
        private Payload payload;
        private Set<EventParameter> eventParameters;

        private Builder() {
        }

        public static Builder aTargetEvent() {
            return new Builder();
        }

        public Builder sourceId(String sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        public Builder targetId(String targetId) {
            this.targetId = targetId;
            return this;
        }

        public Builder destinationId(String destinationId) {
            this.destinationId = destinationId;
            return this;
        }

        public Builder payload(Payload payload) {
            this.payload = payload;
            return this;
        }

        public Builder eventParameters(Set<EventParameter> eventParameters) {
            this.eventParameters = eventParameters;
            return this;
        }

        public TargetEvent build() {
            TargetEvent targetEvent = new TargetEvent();
            targetEvent.id = UUID.randomUUID().toString().replace("-", "");
            targetEvent.sourceId = this.sourceId;
            targetEvent.targetId = this.targetId;
            targetEvent.destinationId = this.destinationId;
            targetEvent.payload = this.payload;
            targetEvent.eventParameters = this.eventParameters;
            return targetEvent;
        }
    }
}
