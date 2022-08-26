package dev.jianmu.trigger.aggregate;

import java.util.List;

/**
 * @class Webhook
 * @description Webhook
 * @author Ethan Liu
 * @create 2021-11-10 19:07
 */
public class Webhook {
    private String only;
    private WebhookAuth auth;
    private List<WebhookParameter> param;
    private List<WebhookEvent> events;

    public String getOnly() {
        return only;
    }

    public WebhookAuth getAuth() {
        return auth;
    }

    public List<WebhookParameter> getParam() {
        return param;
    }

    public List<WebhookEvent> getEvents() {
        return events;
    }

    public static final class Builder {
        private String only;
        private WebhookAuth auth;
        private List<WebhookParameter> param;
        private List<WebhookEvent> events;

        private Builder() {
        }

        public static Builder aWebhook() {
            return new Builder();
        }

        public Builder only(String only) {
            this.only = only;
            return this;
        }

        public Builder auth(WebhookAuth auth) {
            this.auth = auth;
            return this;
        }

        public Builder param(List<WebhookParameter> param) {
            this.param = param;
            return this;
        }

        public Builder events(List<WebhookEvent> events) {
            this.events = events;
            return this;
        }

        public Webhook build() {
            Webhook webhook = new Webhook();
            webhook.only = this.only;
            webhook.auth = this.auth;
            webhook.param = this.param;
            webhook.events = this.events;
            return webhook;
        }
    }
}
