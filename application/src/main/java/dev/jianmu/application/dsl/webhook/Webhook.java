package dev.jianmu.application.dsl.webhook;

import dev.jianmu.trigger.aggregate.WebhookAuth;
import dev.jianmu.trigger.aggregate.WebhookEvent;
import dev.jianmu.trigger.aggregate.WebhookParameter;
import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookInstance;
import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookRule;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class Webhook {
    private List<WebhookParameter> param;
    private WebhookAuth auth;
    private String only;
    private String webhook;
    private List<?> event;

    /**
     * webhookEvent
     */
    private WebhookEvent webhookEvent;

    public void setParam(List<WebhookParameter> param) {
        this.param = param;
    }

    public void setWebhookEvent(WebhookEvent webhookEvent) {
        this.webhookEvent = webhookEvent;
    }

    public List<CustomWebhookInstance.EventInstance> getEventInstances() {
        return this.event.stream()
                .filter(e -> e instanceof Map)
                .map(e -> (Map<String, Object>) e)
                .map(e -> {
                    var ruleset = new ArrayList<CustomWebhookRule>();
                    var ref = (String) e.get("ref");
                    var rules = (List<?>) e.get("ruleset");
                    if (rules != null) {
                        rules.stream().map(rule -> (Map<String, Object>) rule)
                                .forEach(rule -> {
                                    var paramRef = (String) rule.get("param-ref");
                                    var paramOperator = (String) rule.get("operator");
                                    var value = rule.get("value");
                                    ruleset.add(CustomWebhookRule.Builder.aCustomWebhookRule()
                                            .paramRef(paramRef)
                                            .operator(CustomWebhookRule.Operator.valueOf(paramOperator.toUpperCase()))
                                            .matchingValue(value)
                                            .build());
                                });
                    }
                    var rulesetOperator = (String) e.get("ruleset-operator");
                    return CustomWebhookInstance.EventInstance.Builder.aEventInstance()
                            .ref(ref)
                            .ruleset(ruleset)
                            .rulesetOperator(CustomWebhookInstance.RulesetOperator.valueOf(rulesetOperator.toUpperCase()))
                            .build();
                }).collect(Collectors.toList());
    }
}
