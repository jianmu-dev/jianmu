package dev.jianmu.application.dsl.webhook;

import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookInstance;
import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookRule;
import lombok.Getter;

import java.util.List;

@Getter
public class CustomWebhookEvent {
    /**
     * 唯一标识，用于在dsl中指定
     */
    private String ref;
    /**
     * 名称，用于展示
     */
    private String name;

    private List<CustomWebhookRule> ruleset;
    /**
     * 规则集运算符
     */
    private CustomWebhookInstance.RulesetOperator rulesetOperator;
}
