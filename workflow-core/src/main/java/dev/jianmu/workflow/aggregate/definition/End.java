package dev.jianmu.workflow.aggregate.definition;

/**
 * @program: workflow
 * @description 结束节点
 * @author Ethan Liu
 * @create 2021-01-21 13:13
*/
public class End extends BaseNode {

    private End() {
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

        public static Builder anEnd() {
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

        public End build() {
            End end = new End();
            end.name = this.name;
            end.ref = this.ref;
            end.description = this.description;
            return end;
        }
    }
}
