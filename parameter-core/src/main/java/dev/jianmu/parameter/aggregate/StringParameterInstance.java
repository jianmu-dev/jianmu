package dev.jianmu.parameter.aggregate;

/**
 * @class: StringInstance
 * @description: 字符串参数实例
 * @author: Ethan Liu
 * @create: 2021-04-04 11:02
 **/
public class StringParameterInstance extends ParameterInstance<String> {
    public StringParameterInstance(String value) {
        super(value);
    }


    public static final class Builder {
        // 显示名称
        protected String name;
        // 唯一引用名称
        protected String ref;
        // 描述
        protected String description;
        // 外部ID, WorkflowInstanceId or TaskInstanceId
        protected String businessId;
        // 作用域
        protected String scope;
        // 类型
        protected ParameterDefinition.Type type;
        // 参数值
        private String value;

        private Builder() {
        }

        public static Builder aStringParameterInstance() {
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

        public Builder type(ParameterDefinition.Type type) {
            this.type = type;
            return this;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public StringParameterInstance build() {
            StringParameterInstance stringParameterInstance = new StringParameterInstance(this.value);
            stringParameterInstance.scope = this.scope;
            stringParameterInstance.description = this.description;
            stringParameterInstance.name = this.name;
            stringParameterInstance.type = this.type;
            stringParameterInstance.ref = this.ref;
            stringParameterInstance.businessId = this.businessId;
            return stringParameterInstance;
        }
    }
}
