package dev.jianmu.trigger.entity;

/**
 * @class: TriggerEntity
 * @description: TriggerEntity
 * @author: Ethan Liu
 * @create: 2021-05-24 09:51
 **/
public class TriggerEntity {
    private long id;
    private String triggerId;
    private String cron;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(String triggerId) {
        this.triggerId = triggerId;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }


    public static final class Builder {
        private String triggerId;
        private String cron;

        private Builder() {
        }

        public static Builder aTriggerEntity() {
            return new Builder();
        }

        public Builder triggerId(String triggerId) {
            this.triggerId = triggerId;
            return this;
        }

        public Builder cron(String cron) {
            this.cron = cron;
            return this;
        }

        public TriggerEntity build() {
            TriggerEntity triggerEntity = new TriggerEntity();
            triggerEntity.setTriggerId(triggerId);
            triggerEntity.setCron(cron);
            return triggerEntity;
        }
    }
}
