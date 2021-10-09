package dev.jianmu.workflow.aggregate.definition;

/**
 * @class: GlobalParameter
 * @description: 全局参数
 * @author: Ethan Liu
 * @create: 2021-09-04 16:43
 **/
public class GlobalParameter {
    private String name;
    private String value;

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public static final class Builder {
        private String name;
        private String value;

        private Builder() {
        }

        public static Builder aGlobalParameter() {
            return new Builder();
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public GlobalParameter build() {
            GlobalParameter globalParameter = new GlobalParameter();
            globalParameter.value = this.value;
            globalParameter.name = this.name;
            return globalParameter;
        }
    }
}
