package dev.jianmu.trigger.aggregate;

/**
 * @class: WebhookAuth
 * @description: WebhookAuth
 * @author: Ethan Liu
 * @create: 2021-11-10 11:25
 */
public class WebhookAuth {
    private String token;
    private String value;

    public String getToken() {
        return token;
    }

    public String getValue() {
        return value;
    }

    public static final class Builder {
        private String token;
        private String value;

        private Builder() {
        }

        public static Builder aWebhookAuth() {
            return new Builder();
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public WebhookAuth build() {
            WebhookAuth webhookAuth = new WebhookAuth();
            webhookAuth.token = this.token;
            webhookAuth.value = this.value;
            return webhookAuth;
        }
    }

    @Override
    public String toString() {
        return "WebhookAuth{" +
                "token='" + token + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
