package dev.jianmu.application.dsl.webhook;

public class DslWebhookParameter extends dev.jianmu.trigger.aggregate.WebhookParameter {
    private Object value;

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
