package dev.jianmu.trigger.aggregate.custom.webhook;

/**
 * @author laoji
 * @class 自定义Webhook定义
 * @description
 * @create 2022-07-30 23:38
 */
public class CustomWebhookDefinition {
    /**
     * 主键
     */
    private String id;
    /**
     * 唯一标识
     */
    private String ref;
    /**
     * 名称
     */
    private String name;
    /**
     * 描述
     */
    private String description;
    /**
     * 图标
     */
    private String icon;
    /**
     * 拥有者唯一标识
     */
    private String ownerRef;
    /**
     * 拥有者名称
     */
    private String ownerName;
    /**
     * 拥有者类型
     */
    private OwnerType ownerType;
    /**
     * 创建者唯一标识
     */
    private String creatorRef;
    /**
     * 创建者名称
     */
    private String creatorName;

    /**
     * 拥有者类型枚举
     */
    public enum OwnerType {
        /**
         * 组织
         */
        ORGANIZATION,
        /**
         * 个人
         */
        PERSONAL,
        /**
         * 本地
         */
        LOCAL
    }
}
