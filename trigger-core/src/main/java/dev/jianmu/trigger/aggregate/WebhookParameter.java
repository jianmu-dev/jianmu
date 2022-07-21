package dev.jianmu.trigger.aggregate;

/**
 * @class WebhookParameter
 * @description WebhookParameter
 * @author Ethan Liu
 * @create 2021-11-10 11:26
 */
public class WebhookParameter {
    private String ref;
    private String name;
    private String type;
    private Object value;
    private Boolean required = false;
    private Boolean hidden;

    public void setValue(Object value) {
        this.value = value;
    }

    public Boolean getRequired() {
        return required;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getRef() {
        return ref;
    }

    public Object getValue() {
        return value;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public static final class Builder {
        private String ref;
        private String name;
        private String type;
        private Object value;
        private Boolean required;
        private Boolean hidden;

        private Builder() {
        }

        public static Builder aWebhookParameter() {
            return new Builder();
        }

        public Builder ref(String ref) {
            this.ref = ref;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder value(Object value) {
            this.value = value;
            return this;
        }

        public Builder required(Boolean required) {
            this.required = required;
            return this;
        }

        public Builder hidden(Boolean hidden) {
            this.hidden = hidden;
            return this;
        }

        public WebhookParameter build() {
            WebhookParameter webhookParameter = new WebhookParameter();
            webhookParameter.ref = this.ref;
            webhookParameter.type = this.type;
            webhookParameter.name = this.name;
            webhookParameter.value = this.value;
            webhookParameter.required = this.required;
            webhookParameter.hidden = this.hidden;
            return webhookParameter;
        }
    }

    @Override
    public String toString() {
        return "WebhookParameter{" +
                "ref='" + ref + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", value='" + value + '\'' +
                ", required=" + required +
                ", hidden=" + hidden +
                '}';
    }
}
