package dev.jianmu.trigger.aggregate.custom.webhook;

import dev.jianmu.trigger.aggregate.WebhookParameter;

import java.util.List;

/**
 * @author laoji
 * @class 自定义Webhook定义版本
 * @description
 * @create 2022-07-30 23:38
 */
public class CustomWebhookDefinitionVersion {
    /**
     * 主键
     */
    private String id;
    /**
     * 自定义Webhook定义唯一标识
     */
    private String ref;
    /**
     * 自定义Webhook定义拥有者唯一标识
     */
    private String ownerRef;
    /**
     * 版本号
     */
    private String version;
    /**
     * 创建者唯一标识
     */
    private String creatorRef;
    /**
     * 创建者名称
     */
    private String creatorName;
    /**
     * 事件集
     */
    private List<Event> events;

    /**
     * 事件
     */
    public static class Event {
        /**
         * 唯一标识，用于在dsl中指定
         */
        private String ref;
        /**
         * 名称，用于展示
         */
        private String name;
        /**
         * 可用参数
         */
        private List<WebhookParameter> availableParams;
        /**
         * 规则集
         * 规则集运算为AND，不提供OR，若想实现OR效果，定义多个事件
         */
        private List<CustomWebhookRule> ruleset;
    }
}
