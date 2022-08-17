package dev.jianmu.application.dsl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.jianmu.application.exception.DslException;
import dev.jianmu.trigger.aggregate.WebhookParameter;
import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookDefinitionVersion;
import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookRule;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Daihw
 * @class CustomWebhookDslParser
 * @description CustomWebhookDslParser
 * @create 2022/8/17 11:11 上午
 */
@Getter
@NoArgsConstructor
public class CustomWebhookDslParser {
    private String ref;
    private String version;
    private String description;
    private List<Map<String, Object>> event;
    private Object ui;
    private List<CustomWebhookDefinitionVersion.Event> versionEvents = new ArrayList<>();

    public static CustomWebhookDslParser parse(String dslText) {
        var parser = new CustomWebhookDslParser();
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            parser = mapper.readValue(dslText, CustomWebhookDslParser.class);
            parser.createEvent();
        } catch (IOException | DuplicateKeyException e) {
            throw new DslException("DSL解析异常: " + e.getMessage());
        }
        return parser;
    }

    public String getWebhookType() {
        return String.join("@", this.ref, this.version);
    }

    private void createEvent() {
        if (this.event == null) {
            throw new IllegalArgumentException("事件不能为空");
        }
        this.event.stream()
                .forEach(e -> {
                    var ref = (String) e.get("ref");
                    var name = e.get("name");
                    var availableParams = this.createAvailableParam(e.get("param"));
                    var ruleset = this.createRule(e.get("ruleset"));
                    this.versionEvents.add(CustomWebhookDefinitionVersion.Event.Builder.aEvent()
                            .ref(ref)
                            .name(name == null ? ref : (String) name)
                            .availableParams(availableParams)
                            .ruleset(ruleset)
                            .build());
                });
    }

    private List<WebhookParameter> createAvailableParam(Object param) {
        if (param == null) {
            return List.of();
        }
        return ((List<Map<String, Object>>) param).stream()
                .map(p -> {
                    var ref = (String) p.get("ref");
                    var name = p.get("name");
                    var type = (String) p.get("type");
                    var value = p.get("value");
                    var required = p.get("required");
                    var hidden = p.get("hidden");
                    return WebhookParameter.Builder.aWebhookParameter()
                            .ref(ref)
                            .name(name == null ? ref : (String) name)
                            .type(type)
                            .value(value)
                            .required(required == null || (Boolean) required)
                            .hidden(hidden != null && (Boolean) hidden)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<CustomWebhookRule> createRule(Object ruleset) {
        if (ruleset == null) {
            return List.of();
        }
        return ((List<Map<String, Object>>) ruleset).stream()
                .map(r -> {
                    var paramRef = (String) r.get("param-ref");
                    var operator = (String) r.get("operator");
                    var value = r.get("value");
                    return CustomWebhookRule.Builder.aCustomWebhookRule()
                            .paramRef(paramRef)
                            .operator(CustomWebhookRule.Operator.valueOf(operator.toUpperCase()))
                            .matchingValue(value)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
