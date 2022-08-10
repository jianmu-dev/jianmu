package dev.jianmu.workflow.aggregate.definition;


import java.util.Set;

/**
 * @class CustomWebhook
 * @description CustomWebhook
 * @author Daihw
 * @create 2022/8/9 2:23 下午
 */
public class CustomWebhook extends BaseNode implements Trigger {
    private String webhook;

    public String getWebhook() {
        return webhook;
    }

    public static final class Builder {
        // 显示名称
        protected String name;
        // 唯一引用名称
        protected String ref;
        // 描述
        protected String description;
        // 类型
        protected String type;
        // webhook
        protected String webhook;
        // targets
        protected Set<String> targets;
        // 节点元数据快照
        protected String metadata;

        private Builder() {
        }

        public static Builder aCustomWebhook() {
            return new Builder();
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder ref(String ref) {
            this.ref = ref;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder webhook(String webhook) {
            this.webhook = webhook;
            return this;
        }

        public Builder targets(Set<String> targets) {
            this.targets = targets;
            return this;
        }

        public Builder metadata(String metadata) {
            this.metadata = metadata;
            return this;
        }

        public CustomWebhook build() {
            CustomWebhook customWebhook = new CustomWebhook();
            customWebhook.name = this.name;
            customWebhook.ref = this.ref;
            customWebhook.description = this.description;
            customWebhook.type = this.type;
            customWebhook.webhook = this.webhook;
            customWebhook.metadata = this.metadata;
            customWebhook.targets = this.targets;
            return customWebhook;
        }
    }
}
