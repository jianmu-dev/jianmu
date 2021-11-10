package dev.jianmu.trigger.aggregate;

/**
 * @class: WebhookParameter
 * @description: WebhookParameter
 * @author: Ethan Liu
 * @create: 2021-11-10 11:26
 */
public class WebhookParameter {
    private String name;
    private String type;
    private String exp;

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getExp() {
        return exp;
    }

    public static final class Builder {
        private String name;
        private String type;
        private String exp;

        private Builder() {
        }

        public static Builder aWebhookParameter() {
            return new Builder();
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder exp(String exp) {
            this.exp = exp;
            return this;
        }

        public WebhookParameter build() {
            WebhookParameter webhookParameter = new WebhookParameter();
            webhookParameter.type = this.type;
            webhookParameter.name = this.name;
            webhookParameter.exp = this.exp;
            return webhookParameter;
        }
    }

    @Override
    public String toString() {
        return "WebhookParameter{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", exp='" + exp + '\'' +
                '}';
    }
}
