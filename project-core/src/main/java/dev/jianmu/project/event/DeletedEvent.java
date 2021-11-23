package dev.jianmu.project.event;

/**
 * @class DeletedEvent
 * @description DeletedEvent
 * @author Ethan Liu
 * @create 2021-08-21 18:31
*/
public class DeletedEvent {
    private final String projectId;

    public DeletedEvent(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectId() {
        return projectId;
    }
}
