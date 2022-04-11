package dev.jianmu.application.dsl.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WebhookParameter extends dev.jianmu.trigger.aggregate.WebhookParameter {
    private Object value;

    @JsonProperty("value")
    public Object getValue() {
        return value;
    }

    @JsonProperty("default")
    public void setValue(Object value) {
        this.value = value;
    }
}
