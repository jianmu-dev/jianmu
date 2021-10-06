package dev.jianmu.eventbridge.aggregate;

import java.util.Optional;
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
    private String bridgeId;
    private String sourceId;
    private String sourceEventId;
    private String connectionEventId;
    private String targetId;
    private String targetRef;
    private String destinationId;
    private Payload payload;
    private Set<EventParameter> eventParameters;

    public String getId() {
        return id;
    }

    public String getBridgeId() {
        return bridgeId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getSourceEventId() {
        return sourceEventId;
    }

    public String getConnectionEventId() {
        return connectionEventId;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getTargetRef() {
        return targetRef;
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

    public Optional<EventParameter> getEventParameter(String name) {
        return this.eventParameters.stream()
                .filter(eventParameter -> eventParameter.getName().equals(name))
                .findFirst();
    }

    public void setEventParameters(Set<EventParameter> eventParameters) {
        this.eventParameters = eventParameters;
    }

    public static final class Builder {
        private String bridgeId;
        private String sourceId;
        private String sourceEventId;
        private String connectionEventId;
        private String targetId;
        private String targetRef;
        private String destinationId;
        private Payload payload;
        private Set<EventParameter> eventParameters;

        private Builder() {
        }

        public static Builder aTargetEvent() {
            return new Builder();
        }

        public Builder bridgeId(String bridgeId) {
            this.bridgeId = bridgeId;
            return this;
        }

        public Builder sourceEventId(String sourceEventId) {
            this.sourceEventId = sourceEventId;
            return this;
        }

        public Builder connectionEventId(String connectionEventId) {
            this.connectionEventId = connectionEventId;
            return this;
        }

        public Builder sourceId(String sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        public Builder targetId(String targetId) {
            this.targetId = targetId;
            return this;
        }

        public Builder targetRef(String targetRef) {
            this.targetRef = targetRef;
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
            targetEvent.bridgeId = this.bridgeId;
            targetEvent.sourceId = this.sourceId;
            targetEvent.sourceEventId = this.sourceEventId;
            targetEvent.connectionEventId = this.connectionEventId;
            targetEvent.targetId = this.targetId;
            targetEvent.targetRef = this.targetRef;
            targetEvent.destinationId = this.destinationId;
            targetEvent.payload = this.payload;
            targetEvent.eventParameters = this.eventParameters;
            return targetEvent;
        }
    }
}
