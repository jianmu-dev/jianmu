package dev.jianmu.project.aggregate;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @class ProjectLinkGroup
 * @description 项目-项目组中间表
 * @author Daihw
 * @create 2021/11/24 2:54 下午
 */
public class ProjectLinkGroup {
    // ID
    private String id;
    // 项目ID
    private String projectId;
    // 项目组ID
    private String projectGroupId;
    // 排序
    private Integer sort;
    // 创建时间
    private final LocalDateTime createdTime = LocalDateTime.now();

    public String getId() {
        return id;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getProjectGroupId() {
        return projectGroupId;
    }

    public Integer getSort() {
        return sort;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public static class Builder{
        // 项目ID
        private String projectId;
        // 项目组ID
        private String projectGroupId;
        // 排序
        private Integer sort;

        public Builder() {
        }

        public static Builder aReference() {
            return new Builder();
        }

        public Builder projectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder projectGroupId(String projectGroupId) {
            this.projectGroupId = projectGroupId;
            return this;
        }

        public Builder sort(Integer sort) {
            this.sort = sort;
            return this;
        }

        public ProjectLinkGroup build() {
            ProjectLinkGroup projectLinkGroup = new ProjectLinkGroup();
            projectLinkGroup.id = UUID.randomUUID().toString().replace("-", "");
            projectLinkGroup.projectId = this.projectId;
            projectLinkGroup.projectGroupId = this.projectGroupId;
            projectLinkGroup.sort = this.sort;
            return projectLinkGroup;
        }
    }
}
