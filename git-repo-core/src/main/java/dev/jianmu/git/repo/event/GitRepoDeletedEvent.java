package dev.jianmu.git.repo.event;

import dev.jianmu.git.repo.aggregate.Flow;

import java.util.List;

/**
 * @class GitRepoDeletedEvent
 * @description Git仓库删除事件
 * @author Daihw
 * @create 2022/8/18 5:45 下午
 */
public class GitRepoDeletedEvent {
    private String id;
    private List<String> projectIds;

    public String getId() {
        return id;
    }

    public List<String> getProjectIds() {
        return projectIds;
    }

    public static class Builder{
        private String id;
        private List<String> projectIds;

        public static Builder aGetRepoDeletedEvent() {
            return new Builder();
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder projectIds(List<String> projectIds) {
            this.projectIds = projectIds;
            return this;
        }

        public GitRepoDeletedEvent build() {
            var event = new GitRepoDeletedEvent();
            event.id = this.id;
            event.projectIds = this.projectIds;
            return event;
        }
    }
}
