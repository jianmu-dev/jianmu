package dev.jianmu.trigger.aggregate;

import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookRule;

/**
 * @class WebhookRule
 * @description WebhookRule
 * @author Daihw
 * @create 2022/8/24 2:46 下午
 */
public class WebhookRule {
    private String paramRef;
    private String paramName;
    private CustomWebhookRule.Operator operator;
    private Object matchingValue;
    private String expression;
    private Boolean succeed = false;

    public void succeed() {
        this.succeed = true;
    }

    public String getParamRef() {
        return paramRef;
    }

    public String getParamName() {
        return paramName;
    }

    public CustomWebhookRule.Operator getOperator() {
        return operator;
    }

    public Object getMatchingValue() {
        return matchingValue;
    }

    public String getExpression() {
        return expression;
    }

    public Boolean getSucceed() {
        return succeed;
    }

    public static class Builder{
        private String paramRef;
        private String paramName;
        private CustomWebhookRule.Operator operator;
        private Object matchingValue;
        private String expression;

        public static Builder aWebhookRule() {
            return new Builder();
        }

        public Builder paramRef(String paramRef) {
            this.paramRef = paramRef;
            return this;
        }

        public Builder paramName(String paramName) {
            this.paramName = paramName;
            return this;
        }

        public Builder operator(CustomWebhookRule.Operator operator) {
            this.operator = operator;
            return this;
        }

        public Builder matchingValue(Object matchingValue) {
            this.matchingValue = matchingValue;
            return this;
        }

        public Builder expression(String expression) {
            this.expression = expression;
            return this;
        }

        public WebhookRule build() {
            var rule = new WebhookRule();
            rule.paramRef = this.paramRef;
            rule.paramName = this.paramName;
            rule.operator = this.operator;
            rule.matchingValue = this.matchingValue;
            rule.expression = this.expression;
            return rule;
        }
    }
}
