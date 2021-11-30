package dev.jianmu.project.repository;


import dev.jianmu.project.aggregate.ProjectGroup;

import java.util.List;
import java.util.Optional;

/**
 * @author Daihw
 * @class ProjectGroupRepository
 * @description 项目组关联仓储
 * @create 2021/11/24 3:36 下午
 */
public interface ProjectGroupRepository {
    void add(ProjectGroup projectGroup);

    void addAll(List<ProjectGroup> projectGroups);

    void deleteById(String id);

    Optional<ProjectGroup> findById(String id);

    void update(ProjectGroup originProjectGroup);

    List<ProjectGroup> findAllBySortBetween(Integer originSort, Integer targetSort);

    Optional<ProjectGroup> findBySortMax();

    void addProjectCountById(String projectGroupId, int count);

    void subProjectCountById(String projectGroupId, int count);

    void deleteByIdIn(List<String> ids);
}
