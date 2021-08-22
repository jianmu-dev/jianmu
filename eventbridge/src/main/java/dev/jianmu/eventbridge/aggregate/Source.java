package dev.jianmu.eventbridge.aggregate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

/**
 * @class: Source
 * @description: 事件来源
 * @author: Ethan Liu
 * @create: 2021-08-10 11:40
 **/
public class Source {
    public enum Type {
        WEBHOOK,
        SERVICE
    }

    private String id;
    private String name;
    private Type type;
    private String token = "";

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public void generateToken() {
        this.token = UUID.randomUUID().toString().replace("-", "");
    }

    public boolean isValidToken(String token) {
        return this.token.equals(token);
    }

    public String getWebHookUrl() {
        if (token.isBlank()) {
            return token;
        } else {
            var hookId = this.token + "_" + this.id;
            var hook = Base64.getEncoder().encodeToString(hookId.getBytes(StandardCharsets.UTF_8));
            return "/webhook/" + hook;
        }
    }

    public static final class Builder {
        private String name;
        private Type type;

        private Builder() {
        }

        public static Builder aSource() {
            return new Builder();
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        public Source build() {
            Source source = new Source();
            source.id = UUID.randomUUID().toString().replace("-", "");
            source.name = this.name;
            source.type = this.type;
            return source;
        }
    }
}
