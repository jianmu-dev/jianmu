package dev.jianmu.trigger.aggregate;

import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookInstance;

import java.util.List;

public class WebhookEvent {
    private String ref;
    private String name;
    private List<WebhookParameter> availableParams;
    private List<WebhookParameter> eventParams;
    /**
     * 事件规则
     */
    private String only;
    private List<WebhookRule> ruleset;
    private CustomWebhookInstance.RulesetOperator rulesetOperator;

    public String getRef() {
        return ref;
    }

    public String getName() {
        return name;
    }

    public List<WebhookParameter> getEventParams() {
        return eventParams;
    }

    public List<WebhookParameter> getAvailableParams() {
        return availableParams;
    }

    public String getOnly() {
        return only;
    }

    public List<WebhookRule> getRuleset() {
        return ruleset;
    }

    public CustomWebhookInstance.RulesetOperator getRulesetOperator() {
        return rulesetOperator;
    }

    public static class Builder {
        private String ref;
        private String name;
        private List<WebhookParameter> availableParams;
        private List<WebhookParameter> eventParams;
        private String only;
        private List<WebhookRule> ruleset;
        private CustomWebhookInstance.RulesetOperator rulesetOperator;

        public static Builder aWebhookEvent() {
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

        public Builder eventParams(List<WebhookParameter> eventParams) {
            this.eventParams = eventParams;
            return this;
        }

        public Builder availableParams(List<WebhookParameter> availableParams) {
            this.availableParams = availableParams;
            return this;
        }

        public Builder only(String only) {
            this.only = only;
            return this;
        }

        public Builder ruleset(List<WebhookRule> ruleset) {
            this.ruleset = ruleset;
            return this;
        }

        public Builder rulesetOperator(CustomWebhookInstance.RulesetOperator rulesetOperator) {
            this.rulesetOperator = rulesetOperator;
            return this;
        }

        public WebhookEvent build() {
            var webhookEvent = new WebhookEvent();
            webhookEvent.ref = this.ref;
            webhookEvent.name = this.name;
            webhookEvent.availableParams = this.availableParams;
            webhookEvent.eventParams = this.eventParams;
            webhookEvent.only = this.only;
            webhookEvent.ruleset = this.ruleset;
            webhookEvent.rulesetOperator = this.rulesetOperator;
            return webhookEvent;
        }
    }
}
