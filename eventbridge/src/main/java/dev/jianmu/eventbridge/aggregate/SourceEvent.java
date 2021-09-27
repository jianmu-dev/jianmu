package dev.jianmu.eventbridge.aggregate;

import java.util.UUID;

/**
 * @class: OriginalEvent
 * @description: 原始事件
 * @author: Ethan Liu
 * @create: 2021-08-14 17:10
 **/
public class SourceEvent {
    private String id;
    private String bridgeId;
    private String sourceId;
    private Payload payload;

    public String getId() {
        return id;
    }

    public String getBridgeId() {
        return bridgeId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public Payload getPayload() {
        return payload;
    }


    public static final class Builder {
        private String bridgeId;
        private String sourceId;
        private Payload payload;

        private Builder() {
        }

        public static Builder anOriginalEvent() {
            return new Builder();
        }

        public Builder bridgeId(String bridgeId) {
            this.bridgeId = bridgeId;
            return this;
        }

        public Builder sourceId(String sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        public Builder payload(Payload payload) {
            this.payload = payload;
            return this;
        }

        public SourceEvent build() {
            SourceEvent sourceEvent = new SourceEvent();
            sourceEvent.id = UUID.randomUUID().toString().replace("-", "");
            sourceEvent.bridgeId = this.bridgeId;
            sourceEvent.sourceId = this.sourceId;
            sourceEvent.payload = this.payload;
            return sourceEvent;
        }
    }
}
