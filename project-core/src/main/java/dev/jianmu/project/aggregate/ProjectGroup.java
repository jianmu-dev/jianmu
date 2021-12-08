package dev.jianmu.project.aggregate;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @class ProjectGroup
 * @description 建木项目组
 * @author Daihw
 * @create 2021/11/24 2:28 下午
 */
public class ProjectGroup {
    //ID
    private String id = UUID.randomUUID().toString().replace("-", "");
    // 名称
    private String name;
    // 描述
    private String description;
    // 排序
    private Integer sort;
    // 是否展示
    private Boolean isShow;
    // 项目数
    private Integer projectCount = 0;
    // 创建时间
    private LocalDateTime createdTime = LocalDateTime.now();
    // 最后修改时间
    private LocalDateTime lastModifiedTime;

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public void setIsShow(Boolean isShow) {
        this.isShow = isShow;
    }

    public void setProjectCount(Integer projectCount) {
        this.projectCount = projectCount;
    }

    public void setLastModifiedTime() {
        this.lastModifiedTime = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Integer getSort() {
        return sort;
    }

    public Boolean getIsShow() {
        return isShow;
    }

    public Integer getProjectCount() {
        return projectCount;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public LocalDateTime getLastModifiedTime() {
        return lastModifiedTime;
    }

    public static class Builder{
        private String id;
        private String name;
        private String description;
        private Integer sort;
        private Boolean isShow;
        private Integer projectCount;
        private LocalDateTime createdTime;

        public Builder() {
        }

        public static Builder aReference() {
            return new Builder();
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder sort(Integer sort) {
            this.sort = sort;
            return this;
        }

        public Builder isShow(Boolean isShow) {
            this.isShow = isShow;
            return this;
        }

        public Builder projectCount(Integer projectCount) {
            this.projectCount = projectCount;
            return this;
        }

        public Builder createdTime(LocalDateTime createdTime) {
            this.createdTime = createdTime;
            return this;
        }

        public ProjectGroup build() {
            var projectGroup = new ProjectGroup();
            projectGroup.id = this.id;
            projectGroup.name = this.name;
            projectGroup.description = this.description;
            projectGroup.sort = this.sort;
            projectGroup.isShow = this.isShow;
            projectGroup.projectCount = this.projectCount;
            projectGroup.createdTime = this.createdTime;
            projectGroup.lastModifiedTime = LocalDateTime.now();
            return projectGroup;
        }
    }
}
