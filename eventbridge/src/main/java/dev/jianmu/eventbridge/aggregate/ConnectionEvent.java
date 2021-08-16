package dev.jianmu.eventbridge.aggregate;

import java.util.UUID;

/**
 * @class: ConnectionEvent
 * @description: 转发事件
 * @author: Ethan Liu
 * @create: 2021-08-14 17:23
 **/
public class ConnectionEvent {
    private String id;
    private String sourceId;
    private String targetId;
    private Payload payload;

    public String getId() {
        return id;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

    public Payload getPayload() {
        return payload;
    }


    public static final class Builder {
        private String sourceId;
        private String targetId;
        private Payload payload;

        private Builder() {
        }

        public static Builder aConnectionEvent() {
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

        public Builder payload(Payload payload) {
            this.payload = payload;
            return this;
        }

        public ConnectionEvent build() {
            ConnectionEvent connectionEvent = new ConnectionEvent();
            connectionEvent.id = UUID.randomUUID().toString().replace("-", "");
            connectionEvent.sourceId = this.sourceId;
            connectionEvent.targetId = this.targetId;
            connectionEvent.payload = this.payload;
            return connectionEvent;
        }
    }
}
