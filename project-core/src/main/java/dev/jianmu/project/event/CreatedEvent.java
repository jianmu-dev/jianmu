package dev.jianmu.project.event;

/**
 * @class CreatedEvent
 * @description CreatedEvent
 * @author Ethan Liu
 * @create 2021-08-21 18:28
*/
public class CreatedEvent {
    private String projectId;
    private String branch;
    private String associationId;
    private String associationType;

    public String getProjectId() {
        return projectId;
    }

    public String getBranch() {
        return branch;
    }

    public String getAssociationId() {
        return associationId;
    }

    public String getAssociationType() {
        return associationType;
    }

    public static class Builder{
        private String projectId;
        private String branch;
        private String associationId;
        private String associationType;

        public static Builder aCreatedEvent() {
            return new Builder();
        }

        public Builder projectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder branch(String branch) {
            this.branch = branch;
            return this;
        }

        public Builder associationId(String associationId) {
            this.associationId = associationId;
            return this;
        }

        public Builder associationType(String associationType) {
            this.associationType = associationType;
            return this;
        }

        public CreatedEvent build() {
            var event = new CreatedEvent();
            event.projectId = this.projectId;
            event.branch = this.branch;
            event.associationId = this.associationId;
            event.associationType = this.associationType;
            return event;
        }
    }
}
