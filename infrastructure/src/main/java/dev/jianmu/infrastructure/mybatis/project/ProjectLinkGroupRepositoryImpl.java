package dev.jianmu.infrastructure.mybatis.project;

import dev.jianmu.infrastructure.mapper.project.ProjectLinkGroupMapper;
import dev.jianmu.project.aggregate.ProjectLinkGroup;
import dev.jianmu.project.repository.ProjectLinkGroupRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Daihw
 * @class ProjectLinkGroupRepositoryImpl
 * @description 项目-项目组中间表仓储实现
 * @create 2021/11/25 2:37 下午
 */
@Repository
public class ProjectLinkGroupRepositoryImpl implements ProjectLinkGroupRepository {
    private final ProjectLinkGroupMapper projectLinkGroupMapper;

    public ProjectLinkGroupRepositoryImpl(ProjectLinkGroupMapper projectLinkGroupMapper) {
        this.projectLinkGroupMapper = projectLinkGroupMapper;
    }

    @Override
    public void add(ProjectLinkGroup projectLinkGroup) {
        this.projectLinkGroupMapper.add(projectLinkGroup);
    }

    @Override
    public void addAll(List<ProjectLinkGroup> projectLinkGroups) {
        this.projectLinkGroupMapper.addAll(projectLinkGroups);
    }

    @Override
    public List<String> findAllProjectIdByGroupId(String groupId) {
        return this.projectLinkGroupMapper.findAllProjectIdByGroupId(groupId);
    }

    @Override
    public Optional<ProjectLinkGroup> findByProjectGroupIdAndSortMax(String projectGroupId) {
        return this.projectLinkGroupMapper.findByProjectGroupIdAndSortMax(projectGroupId);
    }

    @Override
    public void deleteByProjectGroupId(String projectGroupId) {
        this.projectLinkGroupMapper.deleteByProjectGroupId(projectGroupId);
    }

    @Override
    public Optional<ProjectLinkGroup> findById(String projectLinkGroupId) {
        return this.projectLinkGroupMapper.findById(projectLinkGroupId);
    }

    @Override
    public void deleteById(String projectLinkGroupId) {
        this.projectLinkGroupMapper.deleteById(projectLinkGroupId);
    }

    @Override
    public void deleteByIdIn(List<String> projectLinkGroupIds) {
        if (projectLinkGroupIds.isEmpty()) {
            return;
        }
        this.projectLinkGroupMapper.deleteByIdIn(projectLinkGroupIds);
    }

    @Override
    public List<ProjectLinkGroup> findAllByGroupIdAndSortBetween(String projectGroupId, Integer originSort, Integer targetSort) {
        return this.projectLinkGroupMapper.findAllByGroupIdAndSortBetween(projectGroupId, originSort, targetSort);
    }

    @Override
    public Optional<ProjectLinkGroup> findByProjectId(String projectId) {
        return this.projectLinkGroupMapper.findByProjectId(projectId);
    }

    public Optional<ProjectLinkGroup> findByGroupIdAndProjectId(String groupId, String projectId) {
        return this.projectLinkGroupMapper.findByGroupIdAndProjectId(groupId, projectId);
    }

    @Override
    public List<ProjectLinkGroup> findAllByProjectIdIn(List<String> projectIds) {
        if (projectIds.isEmpty()) {
            return List.of();
        }
        return this.projectLinkGroupMapper.findAllByProjectIdIn(projectIds);
    }
}
