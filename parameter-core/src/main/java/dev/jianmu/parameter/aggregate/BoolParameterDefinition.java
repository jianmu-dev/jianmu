package dev.jianmu.parameter.aggregate;

/**
 * @class: BoolDefinition
 * @description: 布尔参数定义
 * @author: Ethan Liu
 * @create: 2021-04-04 11:00
 **/
public class BoolParameterDefinition extends ParameterDefinition<Boolean> {
    public BoolParameterDefinition(Boolean value) {
        super(value);
    }

    public static final class Builder {
        // 显示名称
        protected String name;
        // 唯一引用名称
        protected String ref;
        // 描述
        protected String description;
        // 外部ID, WorkflowId or TaskDefinitionId
        protected String businessId;
        // 作用域
        protected String scope;
        // 来源
        protected Source source;
        // 类型
        protected Type type;
        // 参数值
        private Boolean value;

        private Builder() {
        }

        public static Builder aBoolDefinition() {
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

        public Builder businessId(String businessId) {
            this.businessId = businessId;
            return this;
        }

        public Builder scope(String scope) {
            this.scope = scope;
            return this;
        }

        public Builder source(Source source) {
            this.source = source;
            return this;
        }

        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        public Builder value(Boolean value) {
            this.value = value;
            return this;
        }

        public BoolParameterDefinition build() {
            BoolParameterDefinition boolDefinition = new BoolParameterDefinition(this.value);
            boolDefinition.ref = this.ref;
            boolDefinition.businessId = this.businessId;
            boolDefinition.source = this.source;
            boolDefinition.name = this.name;
            boolDefinition.scope = this.scope;
            boolDefinition.type = this.type;
            boolDefinition.description = this.description;
            return boolDefinition;
        }
    }
}
