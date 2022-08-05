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

    public CustomWebhookDefinition() {
    }

    public String getId() {
        return id;
    }

    public String getRef() {
        return ref;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    public String getOwnerRef() {
        return ownerRef;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public OwnerType getOwnerType() {
        return ownerType;
    }

    public String getCreatorRef() {
        return creatorRef;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public static class Builder{
        private String id;
        private String ref;
        private String name;
        private String description;
        private String icon;
        private String ownerRef;
        private String ownerName;
        private OwnerType ownerType;
        private String creatorRef;
        private String creatorName;

        public static Builder aCustomWebhookDefinition() {
            return new Builder();
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder ref(String ref) {
            this.ref = ref;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public Builder ownerRef(String ownerRef) {
            this.ownerRef = ownerRef;
            return this;
        }

        public Builder ownerName(String ownerName) {
            this.ownerName = ownerName;
            return this;
        }

        public Builder ownerType(OwnerType ownerType) {
            this.ownerType = ownerType;
            return this;
        }

        public Builder creatorRef(String creatorRef) {
            this.creatorRef = creatorRef;
            return this;
        }

        public Builder creatorName(String creatorName) {
            this.creatorName = creatorName;
            return this;
        }

        public CustomWebhookDefinition build() {
            var customWebhookDefinition = new CustomWebhookDefinition();
            customWebhookDefinition.id = this.id;
            customWebhookDefinition.ref = this.ref;
            customWebhookDefinition.name = this.name;
            customWebhookDefinition.description = this.description;
            customWebhookDefinition.icon = this.icon;
            customWebhookDefinition.ownerRef = this.ownerRef;
            customWebhookDefinition.ownerName = this.ownerName;
            customWebhookDefinition.ownerType = this.ownerType;
            customWebhookDefinition.creatorRef = this.creatorRef;
            customWebhookDefinition.creatorName = this.creatorName;
            return customWebhookDefinition;
        }
    }
}
