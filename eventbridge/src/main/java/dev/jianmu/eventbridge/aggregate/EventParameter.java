package dev.jianmu.eventbridge.aggregate;

/**
 * @class: EventParameter
 * @description: 事件参数
 * @author: Ethan Liu
 * @create: 2021-08-15 10:52
 **/
public class EventParameter {
    private String name;
    private String type;
    private String parameterId;

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getParameterId() {
        return parameterId;
    }

    public static final class Builder {
        private String name;
        private String type;
        private String parameterId;

        private Builder() {
        }

        public static Builder anEventParameter() {
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

        public Builder parameterId(String parameterId) {
            this.parameterId = parameterId;
            return this;
        }

        public EventParameter build() {
            EventParameter eventParameter = new EventParameter();
            eventParameter.name = this.name;
            eventParameter.parameterId = this.parameterId;
            eventParameter.type = this.type;
            return eventParameter;
        }
    }
}
