package dev.jianmu.trigger.event;

/**
 * @author Daihw
 * @class TriggerFailedEvent
 * @description 触发失败事件
 * @create 2022/2/22 10:47 上午
 */
public class TriggerFailedEvent {
    // 触发器ID
    private String triggerId;
    // 触发类型
    private String triggerType;

    public String getTriggerId() {
        return triggerId;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public static final class Builder {
        private String triggerId;
        private String triggerType;

        private Builder() {
        }

        public static Builder aTriggerFailedEvent() {
            return new Builder();
        }

        public Builder triggerId(String triggerId) {
            this.triggerId = triggerId;
            return this;
        }

        public Builder triggerType(String triggerType) {
            this.triggerType = triggerType;
            return this;
        }

        public TriggerFailedEvent build() {
            TriggerFailedEvent triggerFailedEvent = new TriggerFailedEvent();
            triggerFailedEvent.triggerId = this.triggerId;
            triggerFailedEvent.triggerType = this.triggerType;
            return triggerFailedEvent;
        }
    }
}
