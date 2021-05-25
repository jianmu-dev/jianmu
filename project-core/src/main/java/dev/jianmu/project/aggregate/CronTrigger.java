package dev.jianmu.project.aggregate;

import java.util.UUID;

/**
 * @class: CronTrigger
 * @description: CronTrigger
 * @author: Ethan Liu
 * @create: 2021-05-25 11:12
 **/
public class CronTrigger {
    private String id;
    private String projectId;
    private String corn;

    public String getId() {
        return id;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getCorn() {
        return corn;
    }


    public static final class Builder {
        private String projectId;
        private String corn;

        private Builder() {
        }

        public static Builder aCronTrigger() {
            return new Builder();
        }

        public Builder projectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder corn(String corn) {
            this.corn = corn;
            return this;
        }

        public CronTrigger build() {
            CronTrigger cronTrigger = new CronTrigger();
            cronTrigger.id = UUID.randomUUID().toString().replace("-", "");
            cronTrigger.corn = this.corn;
            cronTrigger.projectId = this.projectId;
            return cronTrigger;
        }
    }

    @Override
    public String toString() {
        return "CronTrigger{" +
                "id='" + id + '\'' +
                ", projectId='" + projectId + '\'' +
                ", corn='" + corn + '\'' +
                '}';
    }
}
