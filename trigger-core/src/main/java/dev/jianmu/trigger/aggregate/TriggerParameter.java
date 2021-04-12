package dev.jianmu.trigger.aggregate;

/**
 * @class: TriggerParameter
 * @description: 触发器参数
 * @author: Ethan Liu
 * @create: 2021-04-08 20:13
 **/
public class TriggerParameter {
    // 显示名称
    private String name;
    // 唯一引用名称
    private String ref;
    // 参数类型
    private String type;
    // 描述
    private String description;
    // 参数引用Id
    private String parameterId;
    // 参数值
    private String value;

    private TriggerParameter() {
    }

    public void setParameterId(String parameterId) {
        this.parameterId = parameterId;
    }

    public String getName() {
        return name;
    }

    public String getRef() {
        return ref;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getParameterId() {
        return parameterId;
    }

    public String getValue() {
        return value;
    }

    public static final class Builder {
        // 显示名称
        private String name;
        // 唯一引用名称
        private String ref;
        // 参数类型
        private String type;
        // 描述
        private String description;
        // 参数值
        private String value;

        private Builder() {
        }

        public static Builder aTriggerParameter() {
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

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public TriggerParameter build() {
            TriggerParameter triggerParameter = new TriggerParameter();
            triggerParameter.value = this.value;
            triggerParameter.name = this.name;
            triggerParameter.ref = this.ref;
            triggerParameter.type = this.type;
            triggerParameter.description = this.description;
            return triggerParameter;
        }
    }
}
