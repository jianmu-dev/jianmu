package dev.jianmu.trigger.event;

/**
 * @class TriggerParameter
 * @description TriggerParameter
 * @author Ethan Liu
 * @create 2021-11-11 08:18
 */
public class TriggerEventParameter {
    private String ref;
    private String name;
    private String type;
    private String value;
    private Boolean required;
    private Boolean hidden;
    private String parameterId;

    public void setValue(String value) {
        this.value = value;
    }

    public String getRef() {
        return ref;
    }

    public Boolean getRequired() {
        return required;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public String getParameterId() {
        return parameterId;
    }

    public static final class Builder {
        private String ref;
        private String name;
        private String type;
        private String value;
        private Boolean required;
        private Boolean hidden;
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

        public Builder ref(String ref) {
            this.ref = ref;
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

        public Builder required(Boolean required) {
            this.required = required;
            return this;
        }

        public Builder hidden(Boolean hidden) {
            this.hidden = hidden;
            return this;
        }

        public Builder parameterId(String parameterId) {
            this.parameterId = parameterId;
            return this;
        }

        public TriggerEventParameter build() {
            TriggerEventParameter triggerEventParameter = new TriggerEventParameter();
            triggerEventParameter.ref = this.ref;
            triggerEventParameter.value = this.value;
            triggerEventParameter.parameterId = this.parameterId;
            triggerEventParameter.type = this.type;
            triggerEventParameter.name = this.name;
            triggerEventParameter.required = this.required;
            triggerEventParameter.hidden = this.hidden;
            return triggerEventParameter;
        }
    }
}
