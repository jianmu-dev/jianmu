package dev.jianmu.parameter.aggregate;

/**
 * @class: StringParameter
 * @description: 字符串参数
 * @author: Ethan Liu
 * @create: 2021-02-11 13:59
 **/
public class StringParameter extends Parameter<String> {

    public StringParameter(String value) {
        super(value);
    }


    public static final class Builder {
        // 显示名称
        protected String name;
        // 唯一引用名称
        protected String ref;
        // 参数作用域
        protected Scope scope;
        // 描述
        protected String description;
        // 参数值来源
        protected Source source;
        // 参数值
        private String value;

        private Builder() {
        }

        public static Builder aStringParameter() {
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

        public Builder scope(Scope scope) {
            this.scope = scope;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder source(Source source) {
            this.source = source;
            return this;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public StringParameter build() {
            StringParameter stringParameter = new StringParameter(this.value);
            stringParameter.name = this.name;
            stringParameter.scope = this.scope;
            stringParameter.source = this.source;
            stringParameter.ref = this.ref;
            stringParameter.description = this.description;
            return stringParameter;
        }
    }
}
