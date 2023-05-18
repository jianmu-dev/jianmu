package dev.jianmu.project.event;

import java.util.List;

/**
 * @class DeletedEvent
 * @description DeletedEvent
 * @author Ethan Liu
 * @create 2021-08-21 18:31
*/
public class FileDeletedEvent {
    private String projectId;
    private List<String> triggerIds;
    private List<String> taskInstanceIds;
    private List<String> webRequestIds;

    public FileDeletedEvent() {
    }

    public String getProjectId() {
        return projectId;
    }

    public List<String> getTriggerIds() {
        return triggerIds;
    }

    public List<String> getTaskInstanceIds() {
        return taskInstanceIds;
    }

    public List<String> getWebRequestIds() {
        return webRequestIds;
    }

    public static Builder aFileDeletedEventBuild() {
        return new Builder();
    }

    public static class Builder{
        private String projectId;
        private List<String> triggerIds;
        private List<String> taskInstanceIds;
        private List<String> webRequestIds;

        public Builder projectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder triggerIds(List<String> triggerIds) {
            this.triggerIds = triggerIds;
            return this;
        }

        public Builder taskInstanceIds(List<String> taskInstanceIds) {
            this.taskInstanceIds = taskInstanceIds;
            return this;
        }

        public Builder webRequestIds(List<String> webRequestIds) {
            this.webRequestIds = webRequestIds;
            return this;
        }

        public FileDeletedEvent build() {
            var event = new FileDeletedEvent();
            event.projectId = this.projectId;
            event.triggerIds = this.triggerIds;
            event.taskInstanceIds = this.taskInstanceIds;
            event.webRequestIds = this.webRequestIds;
            return event;
        }
    }
}
