package dev.jianmu.infrastructure.custom_webhook;

import dev.jianmu.trigger.aggregate.WebhookEvent;
import dev.jianmu.trigger.aggregate.WebhookParameter;
import dev.jianmu.trigger.aggregate.WebhookRule;
import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookDefinitionVersion;
import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookInstance;
import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookRule;
import dev.jianmu.trigger.service.WebhookOnlyService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "jianmu.el", name = "type", havingValue = "deno", matchIfMissing = true)
public class JSWebhookOnlyService implements WebhookOnlyService {

    private String getRuleExpression(WebhookParameter webhookParameter, CustomWebhookRule rule) {
        switch (rule.getOperator()) {
            case EQ:
                return webhookParameter.getValue() + " === " + rule.getMatchingValue();
            case NE:
                return webhookParameter.getValue() + " !== " + rule.getMatchingValue();
            case INCLUDE:
                return webhookParameter.getValue() + ".includes(" + rule.getMatchingValue() + ")";
            case EXCLUDE:
                return "!" + webhookParameter.getValue() + ".includes(" + rule.getMatchingValue() + ")";
            case REG_EXP:
            default:
                return "new RegExp(" + rule.getMatchingValue() + ").test(" + webhookParameter.getValue() + ")";
        }
    }

    private WebhookParameter getWebhookParameter(List<WebhookParameter> parameters, CustomWebhookRule rule) {
        return parameters.stream()
                .filter(webhookParameter -> webhookParameter.getRef().equals(rule.getParamRef()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未找到Webhook参数：" + rule.getParamRef()));
    }

    @Override
    public List<WebhookEvent> findEvents(List<CustomWebhookDefinitionVersion.Event> events, List<CustomWebhookInstance.EventInstance> eventInstances) {
        return eventInstances.stream()
                .map(eventInstance -> {
                    var event = events.stream()
                            .filter(e -> e.getRef().equals(eventInstance.getRef()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("未找到Webhook事件：" + eventInstance.getRef()));
                    var only = event.getRuleset().stream()
                            .map(rule -> this.getRuleExpression(this.getWebhookParameter(event.getAvailableParams(), rule), rule))
                            .collect(Collectors.joining(" && "));
                    var postRuleset = eventInstance.getRuleset().stream()
                            .map(rule -> {
                                var webhookParameter = this.getWebhookParameter(event.getAvailableParams(), rule);
                                return WebhookRule.Builder.aWebhookRule()
                                        .paramRef(rule.getParamRef())
                                        .paramName(webhookParameter.getName())
                                        .operator(rule.getOperator())
                                        .matchingValue(rule.getMatchingValue())
                                        .expression(this.getRuleExpression(webhookParameter, rule))
                                        .build();
                            })
                            .collect(Collectors.toList());
                    return WebhookEvent.Builder.aWebhookEvent()
                            .ref(event.getRef())
                            .name(event.getName())
                            .availableParams(event.getAvailableParams())
                            .eventParams(event.findEventParams())
                            .only(only)
                            .ruleset(postRuleset)
                            .rulesetOperator(eventInstance.getRulesetOperator())
                            .build();
                }).collect(Collectors.toList());
    }
}
