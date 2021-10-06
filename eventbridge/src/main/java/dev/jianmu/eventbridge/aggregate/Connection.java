package dev.jianmu.eventbridge.aggregate;

/**
 * @class: Connection
 * @description: Connection
 * @author: Ethan Liu
 * @create: 2021-08-11 12:55
 **/
public class Connection {
    private String id;
    private String bridgeId;
    private String sourceId;
    private String targetId;

    public String getId() {
        return id;
    }

    public String getBridgeId() {
        return bridgeId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

    public static final class Builder {
        private String bridgeId;
        private String sourceId;
        private String targetId;

        private Builder() {
        }

        public static Builder aConnection() {
            return new Builder();
        }

        public Builder bridgeId(String bridgeId) {
            if (bridgeId == null)
                throw new RuntimeException("bridgeId不能为空");
            this.bridgeId = bridgeId;
            return this;
        }

        public Builder sourceId(String sourceId) {
            if (sourceId == null)
                throw new RuntimeException("sourceId不能为空");
            this.sourceId = sourceId;
            return this;
        }

        public Builder targetId(String targetId) {
            this.targetId = targetId;
            return this;
        }

        public Connection build() {
            Connection connection = new Connection();
            connection.id = this.bridgeId + this.sourceId + this.targetId;
            connection.bridgeId = this.bridgeId;
            connection.targetId = this.targetId;
            connection.sourceId = this.sourceId;
            return connection;
        }
    }
}
