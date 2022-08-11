package dev.jianmu.application.dsl;

import dev.jianmu.project.aggregate.Project;
import dev.jianmu.trigger.aggregate.WebhookAuth;
import dev.jianmu.trigger.aggregate.WebhookParameter;
import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookInstance;
import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookRule;
import dev.jianmu.workflow.aggregate.parameter.Parameter;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Daihw
 * @class DslCustomWebhook
 * @description DslCustomWebhook
 * @create 2022/8/9 4:41 下午
 */
@Getter
public class DslCustomWebhook {
    private final List<CustomWebhookInstance.EventInstance> event = new ArrayList<>();
    private Project.TriggerType type;
    private String webhookType;
    private Webhook webhook;
    private String cron;

    public static DslCustomWebhook customWebhook(Map<?, ?> trigger) {
        var dslCustomWebhook = new DslCustomWebhook();
        dslCustomWebhook.type = Project.TriggerType.WEBHOOK;
        dslCustomWebhook.webhookType = (String) trigger.get("webhook");
        var events = (List<?>) trigger.get("event");
        events.stream()
                .filter(event -> event instanceof Map)
                .map(event -> (Map<String, Object>) event)
                .forEach(event -> {
                    var ruleset = new ArrayList<CustomWebhookRule>();
                    var ref = (String) event.get("ref");
                    var rules = (List<?>) event.get("ruleset");
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
                    var rulesetOperator = event.get("ruleset-operator");
                    dslCustomWebhook.event.add(CustomWebhookInstance.EventInstance.Builder.aEventInstance()
                            .ref(ref)
                            .rulesetOperator(CustomWebhookInstance.RulesetOperator.valueOf(((String) rulesetOperator).toUpperCase()))
                            .ruleset(ruleset)
                            .build());
                });
        return dslCustomWebhook;
    }

    public static DslCustomWebhook cron(Map<?, ?> trigger) {
        var dslCustomWebhook = new DslCustomWebhook();
        dslCustomWebhook.type = Project.TriggerType.CRON;
        var schedule = trigger.get("schedule");
        dslCustomWebhook.cron = (String) schedule;
        return dslCustomWebhook;
    }

    public static DslCustomWebhook webhook(Map<?, ?> trigger) {
        var dslCustomWebhook = new DslCustomWebhook();
        dslCustomWebhook.type = Project.TriggerType.WEBHOOK;
        var param = trigger.get("param");
        var auth = trigger.get("auth");
        var only = trigger.get("only");
        var webhookBuilder = Webhook.builder();
        if (only instanceof String) {
            webhookBuilder.only((String) only);
        }
        if (auth instanceof Map) {
            var token = ((Map<?, ?>) auth).get("token");
            var value = ((Map<?, ?>) auth).get("value");
            webhookBuilder.auth(
                    WebhookAuth.Builder.aWebhookAuth()
                            .token((String) token)
                            .value((String) value)
                            .build()
            );
        }
        if (param instanceof List) {
            var ps = ((List<?>) param).stream()
                    .filter(p -> p instanceof Map)
                    .map(p -> (Map<String, Object>) p)
                    .map(p -> {
                        var ref = (String) p.get("ref");
                        var name = p.get("name");
                        var type = p.get("type");
                        var value = p.get("value");
                        var required = p.get("required");
                        var hidden = p.get("hidden");
                        return WebhookParameter.Builder.aWebhookParameter()
                                .ref(ref)
                                .name(name == null ? ref : (String) name)
                                .type(type == null ? Parameter.Type.STRING.name() : (String) type)
                                .value(value)
                                .required(required != null && (Boolean) required)
                                .hidden(hidden != null && (Boolean) hidden)
                                .build();
                    }).collect(Collectors.toList());
            webhookBuilder.param(ps);
        }
        dslCustomWebhook.webhook = webhookBuilder.build();
        return dslCustomWebhook;
    }

    public static Optional<DslCustomWebhook> of(Map<?, ?> trigger) {
        var ref = trigger.get("ref");
        var type = trigger.get("type");
        if (type == null) {
            return Optional.of(customWebhook(trigger));
        }
        return Optional.empty();
    }
}
