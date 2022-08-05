package dev.jianmu.trigger.event;

import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookInstance;

import java.util.List;

public class CustomWebhookInstanceEvent {
    List<CustomWebhookInstance.EventInstance> eventInstances;
    private String triggerId;
    private String webhook;

    public String getTriggerId() {
        return triggerId;
    }

    public String getWebhook() {
        return webhook;
    }

    public List<CustomWebhookInstance.EventInstance> getEventInstances() {
        if (this.eventInstances == null) {
            return List.of();
        }
        return eventInstances;
    }

    public static final class Builder {
        List<CustomWebhookInstance.EventInstance> eventInstances;
        private String triggerId;
        private String webhook;

        private Builder() {
        }

        public static Builder aCustomWebhookInstanceEvent() {
            return new Builder();
        }

        public Builder triggerId(String triggerId) {
            this.triggerId = triggerId;
            return this;
        }

        public Builder webhook(String webhook) {
            this.webhook = webhook;
            return this;
        }

        public Builder eventInstances(List<CustomWebhookInstance.EventInstance> eventInstances) {
            this.eventInstances = eventInstances;
            return this;
        }

        public CustomWebhookInstanceEvent build() {
            CustomWebhookInstanceEvent webhookInstanceEvent = new CustomWebhookInstanceEvent();
            webhookInstanceEvent.triggerId = this.triggerId;
            webhookInstanceEvent.webhook = this.webhook;
            webhookInstanceEvent.eventInstances = this.eventInstances;
            return webhookInstanceEvent;
        }
    }
}
