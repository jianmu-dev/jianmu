package dev.jianmu.node.definition.event;

/**
 * @class NodeDeletedEvent
 * @description NodeDeletedEvent
 * @author Ethan Liu
 * @create 2021-10-07 07:54
*/
public class NodeDeletedEvent {
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

        public static Builder aNodeDeletedEvent() {
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

        public NodeDeletedEvent build() {
            NodeDeletedEvent nodeDeletedEvent = new NodeDeletedEvent();
            nodeDeletedEvent.ownerRef = this.ownerRef;
            nodeDeletedEvent.version = this.version;
            nodeDeletedEvent.ref = this.ref;
            nodeDeletedEvent.spec = this.spec;
            return nodeDeletedEvent;
        }
    }
}
