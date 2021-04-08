package dev.jianmu.parameter.aggregate;

/**
 * @class: StringDefinition
 * @description: 字符串参数定义
 * @author: Ethan Liu
 * @create: 2021-04-04 10:56
 **/
public class StringParameterDefinition extends ParameterDefinition<String> {
    public StringParameterDefinition(String value) {
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
        private String value;

        private Builder() {
        }

        public static Builder aStringDefinition() {
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

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public StringParameterDefinition build() {
            StringParameterDefinition stringDefinition = new StringParameterDefinition(this.value);
            stringDefinition.businessId = this.businessId;
            stringDefinition.ref = this.ref;
            stringDefinition.source = this.source;
            stringDefinition.name = this.name;
            stringDefinition.scope = this.scope;
            stringDefinition.type = this.type;
            stringDefinition.description = this.description;
            return stringDefinition;
        }
    }
}
