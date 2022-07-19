package dev.jianmu.workflow.aggregate.definition;

/**
 * @class GlobalParameter
 * @description 全局参数
 * @author Ethan Liu
 * @create 2021-09-04 16:43
*/
public class GlobalParameter {
    private String ref;
    private String name;
    private String type;
    private Object value;
    private Boolean required;

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getRef() {
        return ref;
    }

    public Object getValue() {
        return value;
    }

    public Boolean getRequired() {
        return required;
    }

    public static final class Builder {
        private String ref;
        private String name;
        private String type;
        private Object value;
        private Boolean required;

        private Builder() {
        }

        public static Builder aGlobalParameter() {
            return new Builder();
        }

        public Builder ref(String ref) {
            this.ref = ref;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder value(Object value) {
            this.value = value;
            return this;
        }

        public Builder required(Boolean required) {
            this.required = required;
            return this;
        }

        public GlobalParameter build() {
            GlobalParameter globalParameter = new GlobalParameter();
            globalParameter.ref = this.ref;
            globalParameter.value = this.value;
            globalParameter.type = this.type;
            globalParameter.name = this.name;
            globalParameter.required = this.required;
            return globalParameter;
        }
    }
}
