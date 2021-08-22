package dev.jianmu.eventbridge.aggregate;

import java.util.UUID;

/**
 * @class: Connection
 * @description: Connection
 * @author: Ethan Liu
 * @create: 2021-08-11 12:55
 **/
public class Connection {
    private String id;
    private String sourceId;
    private String targetId;

    public String getId() {
        return id;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getTargetId() {
        return targetId;
    }


    public static final class Builder {
        private String sourceId;
        private String targetId;

        private Builder() {
        }

        public static Builder aConnection() {
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

        public Connection build() {
            Connection connection = new Connection();
            connection.id = UUID.randomUUID().toString().replace("-", "");
            connection.targetId = this.targetId;
            connection.sourceId = this.sourceId;
            return connection;
        }
    }
}
