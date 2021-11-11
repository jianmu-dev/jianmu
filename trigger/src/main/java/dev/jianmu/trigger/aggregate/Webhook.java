package dev.jianmu.trigger.aggregate;

import java.util.List;

/**
 * @class: Webhook
 * @description: Webhook
 * @author: Ethan Liu
 * @create: 2021-11-10 19:07
 */
public class Webhook {
    private String matcher;
    private WebhookAuth auth;
    private List<WebhookParameter> param;

    public String getMatcher() {
        return matcher;
    }

    public WebhookAuth getAuth() {
        return auth;
    }

    public List<WebhookParameter> getParam() {
        return param;
    }

    public static final class Builder {
        private String matcher;
        private WebhookAuth auth;
        private List<WebhookParameter> param;

        private Builder() {
        }

        public static Builder aWebhook() {
            return new Builder();
        }

        public Builder matcher(String matcher) {
            this.matcher = matcher;
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

        public Webhook build() {
            Webhook webhook = new Webhook();
            webhook.matcher = this.matcher;
            webhook.auth = this.auth;
            webhook.param = this.param;
            return webhook;
        }
    }
}
