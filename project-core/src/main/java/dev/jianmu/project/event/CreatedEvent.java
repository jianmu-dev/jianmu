package dev.jianmu.project.event;

/**
 * @class CreatedEvent
 * @description CreatedEvent
 * @author Ethan Liu
 * @create 2021-08-21 18:28
*/
public class CreatedEvent {
    private final String projectId;

    public CreatedEvent(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectId() {
        return projectId;
    }
}
