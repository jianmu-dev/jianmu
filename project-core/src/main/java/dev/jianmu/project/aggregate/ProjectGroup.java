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
    // 项目数
    private Integer projectCount = 0;
    // 创建时间
    private final LocalDateTime createdTime = LocalDateTime.now();
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

    public void setProjectNumber(Integer projectCount) {
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

    public Integer getProjectNumber() {
        return projectCount;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public LocalDateTime getLastModifiedTime() {
        return lastModifiedTime;
    }
}
