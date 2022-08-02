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
        INCLUDE,
        /**
         * 不包含
         */
        EXCLUDE,
        /**
         * 等于
         */
        EQ,
        /**
         * 不等于
         */
        NE,
        /**
         * 正则表达式
         */
        REG_EXP
    }
}
