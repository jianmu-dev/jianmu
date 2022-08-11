package dev.jianmu.infrastructure.custom_webhook;

import dev.jianmu.trigger.aggregate.WebhookParameter;
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

    @Override
    public String getOnly(List<CustomWebhookDefinitionVersion.Event> events, List<CustomWebhookInstance.EventInstance> eventInstances) {
        return eventInstances.stream()
                .map(eventInstance ->
                        events.stream()
                                .filter(event -> event.getRef().equals(eventInstance.getRef()))
                                .findFirst()
                                .map(event -> {
                                    var pre = event.getRuleset().stream()
                                            .map(rule -> this.getRuleExpression(event.getAvailableParams(), rule))
                                            .collect(Collectors.joining(" && "));
                                    var post = eventInstance.getRuleset().stream()
                                            .map(rule -> this.getRuleExpression(event.getAvailableParams(), rule))
                                            .collect(Collectors.joining(this.getRuleOperator(eventInstance.getRulesetOperator())));
                                    return "(" + pre + ") && (" + post + ")";
                                }).orElseThrow(() -> new IllegalArgumentException("未找到Webhook事件：" + eventInstance.getRef()))
                ).collect(Collectors.joining(" || "));
    }

    private String getRuleExpression(List<WebhookParameter> parameters, CustomWebhookRule rule) {
        var value = parameters.stream()
                .filter(webhookParameter -> webhookParameter.getRef().equals(rule.getParamRef()))
                .findFirst()
                .map(WebhookParameter::getValue)
                .orElseThrow(() -> new IllegalArgumentException("未找到Webhook参数：" + rule.getParamRef()));
        switch (rule.getOperator()) {
            case EQ:
                return value + " === " + rule.getMatchingValue();
            case NE:
                return value + " !== " + rule.getMatchingValue();
            case INCLUDE:
                return value + ".includes(" + rule.getMatchingValue() + ")";
            case EXCLUDE:
                return "!" + value + ".includes(" + rule.getMatchingValue() + ")";
            case REG_EXP:
            default:
                return "new RegExp(" + rule.getMatchingValue() + ").test(" + value + ")";
        }
    }

    private CharSequence getRuleOperator(CustomWebhookInstance.RulesetOperator rulesetOperator) {
        switch (rulesetOperator) {
            case OR:
                return " || ";
            case AND:
            default:
                return " && ";
        }
    }
}
