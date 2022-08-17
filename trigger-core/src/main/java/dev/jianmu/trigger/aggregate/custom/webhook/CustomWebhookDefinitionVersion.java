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
     * 自定义Webhook定义id
     */
    private String definitionId;
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
     * DSL文本
     */
    private String dslText;

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

        public String getRef() {
            return ref;
        }

        public String getName() {
            return name;
        }

        public List<WebhookParameter> getAvailableParams() {
            return availableParams;
        }

        public List<CustomWebhookRule> getRuleset() {
            return ruleset;
        }

        public static class Builder{
            private String ref;
            private String name;
            private List<WebhookParameter> availableParams;
            private List<CustomWebhookRule> ruleset;

            public static Builder aEvent() {
                return new Builder();
            }

            public Builder ref(String ref) {
                this.ref = ref;
                return this;
            }

            public Builder name(String name) {
                this.name = name;
                return this;
            }

            public Builder availableParams(List<WebhookParameter> availableParams) {
                this.availableParams = availableParams;
                return this;
            }

            public Builder ruleset(List<CustomWebhookRule> ruleset) {
                this.ruleset = ruleset;
                return this;
            }

            public Event build() {
                var event = new Event();
                event.ref = this.ref;
                event.name = this.name;
                event.availableParams = this.availableParams;
                event.ruleset = this.ruleset;
                return event;
            }
        }
    }

    public CustomWebhookDefinitionVersion() {
    }

    public String getId() {
        return id;
    }

    public String getDefinitionId() {
        return definitionId;
    }

    public String getRef() {
        return ref;
    }

    public String getOwnerRef() {
        return ownerRef;
    }

    public String getVersion() {
        return version;
    }

    public String getCreatorRef() {
        return creatorRef;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public List<Event> getEvents() {
        return events;
    }

    public String getDslText() {
        return dslText;
    }

    public static class Builder{
        private String id;
        private String definitionId;
        private String ref;
        private String ownerRef;
        private String version;
        private String creatorRef;
        private String creatorName;
        private List<Event> events;
        private String dslText;

        public static Builder aCustomWebhookDefinitionVersion() {
            return new Builder();
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder definitionId(String definitionId) {
            this.definitionId = definitionId;
            return this;
        }

        public Builder ref(String ref) {
            this.ref = ref;
            return this;
        }

        public Builder ownerRef(String ownerRef) {
            this.ownerRef = ownerRef;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
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

        public Builder events(List<Event> events) {
            this.events = events;
            return this;
        }

        public Builder dslText(String dslText) {
            this.dslText = dslText;
            return this;
        }

        public CustomWebhookDefinitionVersion build() {
            var customWebhookDefinitionVersion = new CustomWebhookDefinitionVersion();
            customWebhookDefinitionVersion.id = this.id;
            customWebhookDefinitionVersion.definitionId = this.definitionId;
            customWebhookDefinitionVersion.ref = this.ref;
            customWebhookDefinitionVersion.ownerRef = this.ownerRef;
            customWebhookDefinitionVersion.version = this.version;
            customWebhookDefinitionVersion.creatorRef = this.creatorRef;
            customWebhookDefinitionVersion.creatorName = this.creatorName;
            customWebhookDefinitionVersion.events = this.events;
            customWebhookDefinitionVersion.dslText = this.dslText;
            return customWebhookDefinitionVersion;
        }
    }
}
