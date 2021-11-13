package dev.jianmu.trigger.event;

/**
 * @class: TriggerParameter
 * @description: TriggerParameter
 * @author: Ethan Liu
 * @create: 2021-11-11 08:18
 */
public class TriggerEventParameter {
    private String name;
    private String type;
    private String value;
    private String parameterId;

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getParameterId() {
        return parameterId;
    }

    public static final class Builder {
        private String name;
        private String type;
        private String value;
        private String parameterId;

        private Builder() {
        }

        public static Builder aTriggerParameter() {
            return new Builder();
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Builder parameterId(String parameterId) {
            this.parameterId = parameterId;
            return this;
        }

        public TriggerEventParameter build() {
            TriggerEventParameter triggerEventParameter = new TriggerEventParameter();
            triggerEventParameter.value = this.value;
            triggerEventParameter.parameterId = this.parameterId;
            triggerEventParameter.type = this.type;
            triggerEventParameter.name = this.name;
            return triggerEventParameter;
        }
    }
}
