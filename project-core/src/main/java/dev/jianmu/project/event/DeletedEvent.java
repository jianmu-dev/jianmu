package dev.jianmu.project.event;

/**
 * @class DeletedEvent
 * @description DeletedEvent
 * @author Ethan Liu
 * @create 2021-08-21 18:31
*/
public class DeletedEvent {
    private final String projectId;
    private final String associationId;
    private final String associationType;

    public DeletedEvent(String projectId, String associationId, String associationType) {
        this.projectId = projectId;
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
}
