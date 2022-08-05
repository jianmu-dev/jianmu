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
    private String triggerId;
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

    public CustomWebhookInstance() {
    }

    public String getTriggerId() {
        return triggerId;
    }

    public String getDefinitionId() {
        return definitionId;
    }

    public String getVersionId() {
        return versionId;
    }

    public List<EventInstance> getEventInstances() {
        return eventInstances;
    }

    /**
     * 规则集运算符
     */
    public enum RulesetOperator {
        /**
         * 与
         */
        AND("满足以上所有规则"),
        /**
         * 或
         */
        OR("满足以上任一规则");

        public final String name;

        RulesetOperator(String name) {
            this.name = name;
        }
    }

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

        public String getRef() {
            return ref;
        }

        public List<CustomWebhookRule> getRuleset() {
            return ruleset;
        }

        public RulesetOperator getRulesetOperator() {
            return rulesetOperator;
        }

        public static class Builder {
            private String ref;
            private List<CustomWebhookRule> ruleset;
            private RulesetOperator rulesetOperator;

            public static Builder aEventInstance() {
                return new Builder();
            }

            public Builder ref(String ref) {
                this.ref = ref;
                return this;
            }

            public Builder ruleset(List<CustomWebhookRule> ruleset) {
                this.ruleset = ruleset;
                return this;
            }

            public Builder rulesetOperator(RulesetOperator rulesetOperator) {
                this.rulesetOperator = rulesetOperator;
                return this;
            }

            public EventInstance build() {
                var instance = new EventInstance();
                instance.ref = this.ref;
                instance.ruleset = this.ruleset;
                instance.rulesetOperator = this.rulesetOperator;
                return instance;
            }
        }
    }

    public static class Builder {
        private String triggerId;
        private String definitionId;
        private String versionId;
        private List<EventInstance> eventInstances;

        public static Builder aCustomWebhookInstance() {
            return new Builder();
        }

        public Builder triggerId(String triggerId) {
            this.triggerId = triggerId;
            return this;
        }

        public Builder definitionId(String definitionId) {
            this.definitionId = definitionId;
            return this;
        }

        public Builder versionId(String versionId) {
            this.versionId = versionId;
            return this;
        }

        public Builder eventInstances(List<EventInstance> eventInstances) {
            this.eventInstances = eventInstances;
            return this;
        }

        public CustomWebhookInstance build() {
            var instance = new CustomWebhookInstance();
            instance.triggerId = this.triggerId;
            instance.definitionId = this.definitionId;
            instance.versionId = this.versionId;
            instance.eventInstances = this.eventInstances;
            return instance;
        }
    }
}
