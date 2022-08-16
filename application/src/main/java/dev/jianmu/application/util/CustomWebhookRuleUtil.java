package dev.jianmu.application.util;

import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookRule;
import dev.jianmu.workflow.aggregate.parameter.Parameter;

import java.util.List;

public class CustomWebhookRuleUtil {
    public static List<CustomWebhookRule.Operator> getOperators(Parameter.Type type) {
        switch (type) {
            case NUMBER:
                return List.of(CustomWebhookRule.Operator.EQ, CustomWebhookRule.Operator.NE);
            case BOOL:
                return List.of(CustomWebhookRule.Operator.EQ, CustomWebhookRule.Operator.NE);
            case STRING:
            case SECRET:
            default:
                return List.of(CustomWebhookRule.Operator.INCLUDE, CustomWebhookRule.Operator.EXCLUDE, CustomWebhookRule.Operator.REG_EXP);
        }
    }
}
