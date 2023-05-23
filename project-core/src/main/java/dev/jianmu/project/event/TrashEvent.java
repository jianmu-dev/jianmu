package dev.jianmu.project.event;

/**
 * @author Daihw
 * @class TrashEvent
 * @description TrashEvent
 * @create 2023/5/22 10:26 上午
 */
public class TrashEvent {
    private String projectId;

    public TrashEvent(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectId() {
        return projectId;
    }
}
