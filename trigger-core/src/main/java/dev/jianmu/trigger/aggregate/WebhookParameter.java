package dev.jianmu.trigger.aggregate;

/**
 * @class WebhookParameter
 * @description WebhookParameter
 * @author Ethan Liu
 * @create 2021-11-10 11:26
 */
public class WebhookParameter {
    private String name;
    private String type;
    private String exp;
    private Boolean required = false;
    private Object defaultValue;

    public boolean isRequired() {
        return required;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

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
        private Boolean required;
        private Object defaultValue;

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

        public Builder required(Boolean required) {
            this.required = required;
            return this;
        }

        public Builder defaultVault(Object defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public WebhookParameter build() {
            WebhookParameter webhookParameter = new WebhookParameter();
            webhookParameter.type = this.type;
            webhookParameter.name = this.name;
            webhookParameter.exp = this.exp;
            webhookParameter.required = this.required;
            webhookParameter.defaultValue = this.defaultValue;
            return webhookParameter;
        }
    }

    @Override
    public String toString() {
        return "WebhookParameter{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", exp='" + exp + '\'' +
                ", required=" + required +
                ", defaultValue=" + defaultValue +
                '}';
    }
}
