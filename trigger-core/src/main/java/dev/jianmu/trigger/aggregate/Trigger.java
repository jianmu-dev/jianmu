package dev.jianmu.trigger.aggregate;

import java.util.UUID;

/**
 * @class Trigger
 * @description Trigger
 * @author Ethan Liu
 * @create 2021-11-10 11:06
 */
public class Trigger {
    public enum Type {
        CRON,
        WEBHOOK
    }

    private String id;
    private String projectId;
    private Type type;
    private String schedule;
    private Webhook webhook;

    public String getId() {
        return id;
    }

    public String getProjectId() {
        return projectId;
    }

    public Type getType() {
        return type;
    }

    public String getSchedule() {
        return schedule;
    }

    public Webhook getWebhook() {
        return webhook;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public void setWebhook(Webhook webhook) {
        this.webhook = webhook;
    }

    public static final class Builder {
        private String projectId;
        private Type type;
        private String schedule;
        private Webhook webhook;

        private Builder() {
        }

        public static Builder aTrigger() {
            return new Builder();
        }

        public Builder projectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        public Builder schedule(String schedule) {
            this.schedule = schedule;
            return this;
        }

        public Builder webhook(Webhook webhook) {
            this.webhook = webhook;
            return this;
        }

        public Trigger build() {
            Trigger trigger = new Trigger();
            trigger.id = UUID.randomUUID().toString().replace("-", "");
            trigger.schedule = this.schedule;
            trigger.webhook = this.webhook;
            trigger.projectId = this.projectId;
            trigger.type = this.type;
            return trigger;
        }
    }
}
