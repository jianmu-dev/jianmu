package dev.jianmu.application.util;

import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookRule;
import dev.jianmu.workflow.aggregate.parameter.Parameter;

import java.util.List;

public class CustomWebhookRuleUtil {
    public static List<CustomWebhookRule.Operator> getOperators(Parameter.Type type) {
        switch (type) {
            case NUMBER:
                return List.of(CustomWebhookRule.Operator.NE, CustomWebhookRule.Operator.EQ);
            case BOOL:
                return List.of(CustomWebhookRule.Operator.NE, CustomWebhookRule.Operator.EQ);
            case STRING:
            case SECRET:
            default:
                return List.of(CustomWebhookRule.Operator.EXCLUDE, CustomWebhookRule.Operator.INCLUDE, CustomWebhookRule.Operator.REG_EXP);
        }
    }
}
