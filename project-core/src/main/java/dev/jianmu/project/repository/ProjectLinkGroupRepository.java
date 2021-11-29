package dev.jianmu.project.repository;

import dev.jianmu.project.aggregate.ProjectLinkGroup;

import java.util.List;
import java.util.Optional;

/**
 * @author Daihw
 * @class ProjectLinkGroupRepository
 * @description 项目-项目组关联仓储
 * @create 2021/11/24 3:38 下午
 */
public interface ProjectLinkGroupRepository {
    void add(ProjectLinkGroup projectLinkGroup);

    void addAll(List<ProjectLinkGroup> projectLinkGroups);

    List<String> findAllProjectIdByGroupId(String groupId);

    List<ProjectLinkGroup> findAllByGroupId(String groupId);

    Optional<ProjectLinkGroup> findByProjectGroupIdAndSortMax(String projectGroupId);

    void deleteByProjectGroupId(String projectGroupId);

    Optional<ProjectLinkGroup> findById(String projectLinkGroupId);

    void deleteById(String projectLinkGroupId);

    void deleteByGroupIdAndProjectIdIn(String projectGroupId, List<String> projectIds);

    List<ProjectLinkGroup> findAllByGroupIdAndSortBetween(String projectGroupId, Integer originSort, Integer targetSort);

    void updateSortById(String projectLinkGroupId, Integer sort);

    Optional<ProjectLinkGroup> findByProjectId(String projectId);
}
