package dev.jianmu.eventbridge.aggregate;

import java.util.List;
import java.util.Map;

/**
 * @class: Payload
 * @description: 事件载荷
 * @author: Ethan Liu
 * @create: 2021-08-11 13:23
 **/
public class Payload {
    private Map<String, List<String>> header;
    private Map<String, String[]> query;
    private String path;
    private String body;

    public Map<String, List<String>> getHeader() {
        return header;
    }

    public Map<String, String[]> getQuery() {
        return query;
    }

    public String getPath() {
        return path;
    }

    public String getBody() {
        return body;
    }


    public static final class Builder {
        private Map<String, List<String>> header;
        private Map<String, String[]> query;
        private String path;
        private String body;

        private Builder() {
        }

        public static Builder aPayload() {
            return new Builder();
        }

        public Builder header(Map<String, List<String>> header) {
            this.header = header;
            return this;
        }

        public Builder query(Map<String, String[]> query) {
            this.query = query;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Payload build() {
            Payload payload = new Payload();
            payload.body = this.body;
            payload.header = this.header;
            payload.query = this.query;
            payload.path = this.path;
            return payload;
        }
    }
}
