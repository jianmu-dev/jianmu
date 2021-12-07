package dev.jianmu.application.dsl.webhook;

import lombok.Getter;

@Getter
public class WebhookParameter extends dev.jianmu.trigger.aggregate.WebhookParameter {
    private Object value;

    public void setValue(Object value) {
        this.value = value;
    }
}
