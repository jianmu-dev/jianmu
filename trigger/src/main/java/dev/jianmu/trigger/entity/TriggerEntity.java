package dev.jianmu.trigger.entity;

/**
 * @class: TriggerEntity
 * @description: TriggerEntity
 * @author: Ethan Liu
 * @create: 2021-05-24 09:51
 **/
public class TriggerEntity {
    private long id;
    private String projectId;
    private String cron;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }


    public static final class Builder {
        private String projectId;
        private String cron;

        private Builder() {
        }

        public static Builder aTriggerEntity() {
            return new Builder();
        }

        public Builder projectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder cron(String cron) {
            this.cron = cron;
            return this;
        }

        public TriggerEntity build() {
            TriggerEntity triggerEntity = new TriggerEntity();
            triggerEntity.setProjectId(projectId);
            triggerEntity.setCron(cron);
            return triggerEntity;
        }
    }
}
