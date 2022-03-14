package dev.jianmu.project.event;

/**
 * @class MovedEvent
 * @description MovedEvent
 * @author Daihw
 * @create 2022/3/14 3:21 下午
 */
public class MovedEvent {
    private String projectId;
    private String projectGroupId;

    public MovedEvent(String projectId, String projectGroupId) {
        this.projectId = projectId;
        this.projectGroupId = projectGroupId;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getProjectGroupId() {
        return projectGroupId;
    }
}
