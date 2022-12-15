package dev.jianmu.project.event;

/**
 * @author Ethan Liu
 * @class DeletedEvent
 * @description DeletedEvent
 * @create 2021-08-21 18:31
 */
public class DeletedEvent {
    private String projectId;
    private String userId;
    private String associationId;
    private String associationType;

    public String getProjectId() {
        return projectId;
    }

    public String getAssociationId() {
        return associationId;
    }

    public String getAssociationType() {
        return associationType;
    }

    public String getUserId() {
        return userId;
    }

    public static class Builder{
        private String projectId;
        private String userId;
        private String associationId;
        private String associationType;

        public static Builder aDeletedEvent() {
            return new Builder();
        }

        public Builder projectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
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

        public DeletedEvent build() {
            var event = new DeletedEvent();
            event.projectId = this.projectId;
            event.userId = this.userId;
            event.associationId = this.associationId;
            event.associationType = this.associationType;
            return event;
        }
    }
}
