package dev.jianmu.node.definition.event;

/**
 * @class: NodeUpdatedEvent
 * @description: NodeUpdatedEvent
 * @author: Ethan Liu
 * @create: 2021-10-08 16:21
 **/
public class NodeUpdatedEvent {
    private String ownerRef;
    private String ref;
    private String version;
    private String spec;

    public String getOwnerRef() {
        return ownerRef;
    }

    public String getRef() {
        return ref;
    }

    public String getVersion() {
        return version;
    }

    public String getSpec() {
        return spec;
    }


    public static final class Builder {
        private String ownerRef;
        private String ref;
        private String version;
        private String spec;

        private Builder() {
        }

        public static Builder aNodeUpdatedEvent() {
            return new Builder();
        }

        public Builder ownerRef(String ownerRef) {
            this.ownerRef = ownerRef;
            return this;
        }

        public Builder ref(String ref) {
            this.ref = ref;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder spec(String spec) {
            this.spec = spec;
            return this;
        }

        public NodeUpdatedEvent build() {
            NodeUpdatedEvent nodeUpdatedEvent = new NodeUpdatedEvent();
            nodeUpdatedEvent.ref = this.ref;
            nodeUpdatedEvent.spec = this.spec;
            nodeUpdatedEvent.version = this.version;
            nodeUpdatedEvent.ownerRef = this.ownerRef;
            return nodeUpdatedEvent;
        }
    }
}
