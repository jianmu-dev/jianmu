package dev.jianmu.application.dsl.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class DslWebhookParameter extends dev.jianmu.trigger.aggregate.WebhookParameter {
    private Object value;
    private Object defaultValue;

    public void setValue(Object value) {
        this.value = value;
    }

    @JsonProperty("default")
    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }
}
