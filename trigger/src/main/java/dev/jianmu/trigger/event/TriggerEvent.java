package dev.jianmu.trigger.event;

/**
 * @class: TriggerEvent
 * @description: TriggerEvent
 * @author: Ethan Liu
 * @create: 2021-05-25 08:25
 **/
public class TriggerEvent {
    private String triggerId;

    public String getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(String triggerId) {
        this.triggerId = triggerId;
    }

    public static final class Builder {
        private String triggerId;

        private Builder() {
        }

        public static Builder aTriggerEvent() {
            return new Builder();
        }

        public Builder triggerId(String triggerId) {
            this.triggerId = triggerId;
            return this;
        }

        public TriggerEvent build() {
            TriggerEvent triggerEvent = new TriggerEvent();
            triggerEvent.setTriggerId(triggerId);
            return triggerEvent;
        }
    }
}
