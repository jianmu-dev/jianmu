package dev.jianmu.trigger.aggregate.custom.webhook;

import java.util.List;

/**
 * @author laoji
 * @class 自定义Webhook实例
 * @description
 * @create 2022-07-30 23:38
 */
public class CustomWebhookInstance {
    /**
     * 主键
     */
    private String id;
    /**
     * 自定义Webhook定义id
     */
    private String definitionId;
    /**
     * 自定义Webhook定义版本id
     */
    private String versionId;
    /**
     * 事件实例集
     */
    private List<EventInstance> eventInstances;

    /**
     * 事件实例
     */
    public static class EventInstance {
        /**
         * 事件唯一标识，用于在dsl中指定
         */
        private String ref;
        /**
         * 规则集
         * 基于事件的可用参数构造
         */
        private List<CustomWebhookRule> ruleset;
        /**
         * 规则集运算符
         */
        private RulesetOperator rulesetOperator;
    }

    /**
     * 规则集运算符
     */
    public enum RulesetOperator {
        /**
         * 与
         */
        AND,
        /**
         * 或
         */
        OR
    }
}
