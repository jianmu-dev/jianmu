package dev.jianmu.application.dsl.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
public class DslWebhookParameter {
    private String name;
    private String type;
    private String exp;
    private Boolean required = false;
    @JsonProperty("default")
    private Object defaultValue;
    @Setter
    private Object value;
}
