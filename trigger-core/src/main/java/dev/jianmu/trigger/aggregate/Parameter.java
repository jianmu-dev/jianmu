package dev.jianmu.trigger.aggregate;

/**
 * @class: Parameter
 * @description: 触发器参数
 * @author: Ethan Liu
 * @create: 2021-04-08 20:13
 **/
public class Parameter {
    // 显示名称
    private String name;
    // 唯一引用名称
    private String ref;
    // 描述
    private String description;
    // 参数值引用
    private String valueRef;

    private Parameter() {
    }

    public String getName() {
        return name;
    }

    public String getRef() {
        return ref;
    }

    public String getDescription() {
        return description;
    }

    public String getValueRef() {
        return valueRef;
    }

    public static final class Builder {
        // 显示名称
        private String name;
        // 唯一引用名称
        private String ref;
        // 描述
        private String description;
        // 参数值引用
        private String valueRef;

        private Builder() {
        }

        public static Builder aParameter() {
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

        public Builder valueRef(String valueRef) {
            this.valueRef = valueRef;
            return this;
        }

        public Parameter build() {
            Parameter parameter = new Parameter();
            parameter.ref = this.ref;
            parameter.valueRef = this.valueRef;
            parameter.name = this.name;
            parameter.description = this.description;
            return parameter;
        }
    }
}
