package dev.jianmu.workflow.aggregate.definition;

/**
 * @program: workflow
 * @description 开始节点
 * @author Ethan Liu
 * @create 2021-01-21 13:13
*/
public final class Start extends BaseNode {

    private Start() {
        this.type = this.getClass().getSimpleName();
    }

    public static final class Builder {
        // 显示名称
        protected String name;
        // 唯一引用名称
        protected String ref;
        // 描述
        protected String description;

        private Builder() {
        }

        public static Builder aStart() {
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

        public Start build() {
            Start start = new Start();
            start.name = this.name;
            start.ref = this.ref;
            start.description = this.description;
            return start;
        }
    }
}
