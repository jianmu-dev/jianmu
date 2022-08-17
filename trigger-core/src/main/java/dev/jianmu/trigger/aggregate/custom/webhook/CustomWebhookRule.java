package dev.jianmu.trigger.aggregate.custom.webhook;

/**
 * @author laoji
 * @class 自定义Webhook规则
 * @description
 * @create 2022-07-31 15:43
 */
public class CustomWebhookRule {
    /**
     * 参数
     */
    private String paramRef;
    /**
     * 运算符
     */
    private Operator operator;
    /**
     * 匹配值，可为表达式
     */
    private Object matchingValue;

    /**
     * 运算符枚举
     */
    public enum Operator {
        /**
         * 包含
         */
        INCLUDE("包含"),
        /**
         * 不包含
         */
        EXCLUDE("不包含"),
        /**
         * 等于
         */
        EQ("等于"),
        /**
         * 不等于
         */
        NE("不等于"),
        /**
         * 正则表达式
         */
        REG_EXP("正则匹配");

        public final String name;

        Operator(String name) {
            this.name = name;
        }
    }

    public CustomWebhookRule() {
    }

    public String getParamRef() {
        return paramRef;
    }

    public Operator getOperator() {
        return operator;
    }

    public Object getMatchingValue() {
        return matchingValue;
    }

    public static class Builder{
        private String paramRef;
        private Operator operator;
        private Object matchingValue;

        public static Builder aCustomWebhookRule() {
            return new Builder();
        }

        public Builder paramRef(String paramRef) {
            this.paramRef = paramRef;
            return this;
        }

        public Builder operator(Operator operator) {
            this.operator = operator;
            return this;
        }

        public Builder matchingValue(Object matchingValue) {
            this.matchingValue = matchingValue;
            return this;
        }

        public CustomWebhookRule build() {
            var rule = new CustomWebhookRule();
            rule.paramRef = this.paramRef;
            rule.operator = this.operator;
            rule.matchingValue = this.matchingValue;
            return rule;
        }
    }
}
