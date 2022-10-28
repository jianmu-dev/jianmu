package dev.jianmu.project.event;

/**
 * @author Ethan Liu
 * @class DeletedEvent
 * @description DeletedEvent
 * @create 2021-08-21 18:31
 */
public class DeletedEvent {
    private final String projectId;
    private final String userId;
    private final String associationId;
    private final String associationType;

    public DeletedEvent(String projectId, String userId, String associationId, String associationType) {
        this.projectId = projectId;
        this.userId = userId;
        this.associationId = associationId;
        this.associationType = associationType;
    }

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
}
