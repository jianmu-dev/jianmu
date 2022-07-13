package dev.jianmu.project.event;

/**
 * @class CreatedEvent
 * @description CreatedEvent
 * @author Ethan Liu
 * @create 2021-08-21 18:28
*/
public class CreatedEvent {
    private final String projectId;
    private final String branch;

    public CreatedEvent(String projectId, String branch) {
        this.projectId = projectId;
        this.branch = branch;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getBranch() {
        return branch;
    }
}
