package dev.jianmu.parameter.aggregate;

/**
 * @class: BoolParameter
 * @description: 布尔参数
 * @author: Ethan Liu
 * @create: 2021-02-11 14:52
 **/
public class BoolParameter extends Parameter<Boolean> {

    public BoolParameter(Boolean value) {
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
        private Boolean value;

        private Builder() {
        }

        public static Builder aBoolParameter() {
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

        public Builder value(Boolean value) {
            this.value = value;
            return this;
        }

        public BoolParameter build() {
            BoolParameter boolParameter = new BoolParameter(this.value);
            boolParameter.name = this.name;
            boolParameter.scope = this.scope;
            boolParameter.source = this.source;
            boolParameter.description = this.description;
            boolParameter.ref = this.ref;
            return boolParameter;
        }
    }
}
