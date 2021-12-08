package dev.jianmu.application.service;

import com.github.pagehelper.PageInfo;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.exception.ProjectGroupException;
import dev.jianmu.infrastructure.mybatis.project.ProjectLinkGroupRepositoryImpl;
import dev.jianmu.project.aggregate.ProjectGroup;
import dev.jianmu.project.aggregate.ProjectLinkGroup;
import dev.jianmu.project.repository.ProjectGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Daihw
 * @class ProjectGroupApplication
 * @description 项目组门面类
 * @create 2021/11/25 2:59 下午
 */
@Service
public class ProjectGroupApplication {
    public static final String DEFAULT_PROJECT_GROUP_NAME = "默认分组";

    private final ProjectGroupRepository projectGroupRepository;
    private final ProjectLinkGroupRepositoryImpl projectLinkGroupRepository;

    public ProjectGroupApplication(ProjectGroupRepository projectGroupRepository, ProjectLinkGroupRepositoryImpl projectLinkGroupRepository) {
        this.projectGroupRepository = projectGroupRepository;
        this.projectLinkGroupRepository = projectLinkGroupRepository;
    }

    public List<ProjectGroup> findAll() {
        return this.projectGroupRepository.findAll();
    }

    public ProjectGroup findById(String projectGroupId) {
        return this.projectGroupRepository.findById(projectGroupId)
                .orElseThrow(() -> new DataNotFoundException("未找到项目组"));
    }

    @Transactional
    public void createProjectGroup(ProjectGroup projectGroup) {
        var sort = this.projectGroupRepository.findBySortMax()
                .map(ProjectGroup::getSort)
                .orElseThrow(() -> new DataNotFoundException("未找到默认项目组"));
        projectGroup.setProjectCount(0);
        projectGroup.setSort(++sort);
        projectGroup.setLastModifiedTime();
        this.projectGroupRepository.add(projectGroup);
    }

    @Transactional
    public void updateProjectGroup(String projectGroupId, ProjectGroup projectGroup) {
        var originProjectGroup = this.projectGroupRepository.findById(projectGroupId)
                .orElseThrow(() -> new DataNotFoundException("未找到项目组"));
        if (!DEFAULT_PROJECT_GROUP_NAME.equals(originProjectGroup.getName())) {
            originProjectGroup.setName(projectGroup.getName());
        }
        originProjectGroup.setDescription(projectGroup.getDescription());
        originProjectGroup.setLastModifiedTime();
        this.projectGroupRepository.update(originProjectGroup);
    }

    @Transactional
    public void deleteById(String projectGroupId) {
        var defaultGroupId = this.projectGroupRepository.findByName(DEFAULT_PROJECT_GROUP_NAME).map(ProjectGroup::getId)
                .orElseThrow(() -> new DataNotFoundException("未找到默认项目组"));
        if (projectGroupId.equals(defaultGroupId)) {
            throw new ProjectGroupException("不能删除默认分组");
        }
        var projectIds = this.projectLinkGroupRepository.findAllProjectIdByGroupId(projectGroupId);
        // 删除分组中间表
        this.projectLinkGroupRepository.deleteByProjectGroupId(projectGroupId);
        // 添加到默认分组
        var sort = this.projectLinkGroupRepository.findByProjectGroupIdAndSortMax(defaultGroupId)
                .map(ProjectLinkGroup::getSort)
                .orElse(-1);
        List<ProjectLinkGroup> projectLinkGroups = new ArrayList<>();
        for (String projectId : projectIds) {
            projectLinkGroups.add(ProjectLinkGroup.Builder.aReference()
                    .projectGroupId(defaultGroupId)
                    .projectId(projectId)
                    .sort(++sort)
                    .build());
        }
        if (!projectLinkGroups.isEmpty()) {
            this.projectLinkGroupRepository.addAll(projectLinkGroups);
            this.projectGroupRepository.addProjectCountById(defaultGroupId, projectLinkGroups.size());
        }
        // 删除项目组
        this.projectGroupRepository.deleteById(projectGroupId);
    }

    @Transactional
    public void updateSort(Integer originSort, Integer targetSort) {
        var groups = this.projectGroupRepository.findAllBySortBetween(Math.min(originSort, targetSort), Math.max(originSort, targetSort));
        if (groups.isEmpty()) {
            return;
        }
        // 删除项目组
        this.projectGroupRepository.deleteByIdIn(groups.stream().map(ProjectGroup::getId).collect(Collectors.toList()));
        // 添加项目组
        var projectGroups = new ArrayList<ProjectGroup>();
        ProjectGroup targetProjectGroup;
        if (originSort > targetSort) {
            for (int i = 0; i < groups.size() - 1; i++) {
                ProjectGroup projectGroup = groups.get(i);
                projectGroups.add(ProjectGroup.Builder.aReference()
                        .id(projectGroup.getId())
                        .name(projectGroup.getName())
                        .description(projectGroup.getDescription())
                        .sort(groups.get(i + 1).getSort())
                        .projectCount(projectGroup.getProjectCount())
                        .createdTime(projectGroup.getCreatedTime())
                        .build());
            }
            targetProjectGroup = groups.get(groups.size() - 1);
        } else {
            for (int i = 1; i < groups.size(); i++) {
                ProjectGroup projectGroup = groups.get(i);
                projectGroups.add(ProjectGroup.Builder.aReference()
                        .id(projectGroup.getId())
                        .name(projectGroup.getName())
                        .description(projectGroup.getDescription())
                        .sort(groups.get(i - 1).getSort())
                        .projectCount(projectGroup.getProjectCount())
                        .createdTime(projectGroup.getCreatedTime())
                        .build());
            }
            targetProjectGroup = groups.get(0);
        }
        projectGroups.add(ProjectGroup.Builder.aReference()
                .id(targetProjectGroup.getId())
                .name(targetProjectGroup.getName())
                .description(targetProjectGroup.getDescription())
                .sort(targetSort)
                .projectCount(targetProjectGroup.getProjectCount())
                .createdTime(targetProjectGroup.getCreatedTime())
                .build());
        this.projectGroupRepository.addAll(projectGroups);
    }

    @Transactional
    public void addProject(String projectGroupId, List<String> projectIds) {
        // 删除分组中间表
        var projectLinkGroups = this.projectLinkGroupRepository.findAllByProjectIdIn(projectIds);
        this.projectLinkGroupRepository.deleteByIdIn(projectLinkGroups.stream()
                .map(ProjectLinkGroup::getId)
                .collect(Collectors.toList()));
        // 添加分组中间表
        var sort = this.projectLinkGroupRepository.findByProjectGroupIdAndSortMax(projectGroupId)
                .map(ProjectLinkGroup::getSort)
                .orElse(-1);
        var newProjectLinkGroups = new ArrayList<ProjectLinkGroup>();
        for (String projectId : projectIds) {
            newProjectLinkGroups.add(ProjectLinkGroup.Builder.aReference()
                    .projectGroupId(projectGroupId)
                    .projectId(projectId)
                    .sort(++sort)
                    .build());
        }
        this.projectLinkGroupRepository.addAll(newProjectLinkGroups);
        // 修改项目组个数
        var groupCountMap = projectLinkGroups.stream()
                .collect(Collectors.groupingBy(ProjectLinkGroup::getProjectGroupId, Collectors.counting()));
        groupCountMap.forEach((key, value) -> this.projectGroupRepository.subProjectCountById(key, Math.toIntExact(value)));
        this.projectGroupRepository.addProjectCountById(projectGroupId, projectIds.size());
    }

    @Transactional
    public void deleteProject(String projectLinkGroupId) {
        var defaultGroupId = this.projectGroupRepository.findByName(DEFAULT_PROJECT_GROUP_NAME).map(ProjectGroup::getId)
                .orElseThrow(() -> new DataNotFoundException("未找到默认项目组"));
        var projectLinkGroup = this.projectLinkGroupRepository.findById(projectLinkGroupId)
                .orElseThrow(() -> new DataNotFoundException("未找到项目组中的项目"));
        if (projectLinkGroup.getProjectGroupId().equals(defaultGroupId)) {
            throw new ProjectGroupException("不能删除默认分组的项目");
        }
        // 删除分组中间表
        this.projectLinkGroupRepository.deleteById(projectLinkGroupId);
        // 添加到默认分组
        var sort = this.projectLinkGroupRepository.findByProjectGroupIdAndSortMax(defaultGroupId)
                .map(ProjectLinkGroup::getSort)
                .orElseThrow(() -> new DataNotFoundException("未找到默认项目组"));
        var newProjectLinkGroup = ProjectLinkGroup.Builder.aReference()
                .projectGroupId(defaultGroupId)
                .projectId(projectLinkGroup.getProjectId())
                .sort(++sort)
                .build();
        this.projectLinkGroupRepository.add(newProjectLinkGroup);
        // 修改项目组个数
        this.projectGroupRepository.subProjectCountById(projectLinkGroup.getProjectGroupId(), 1);
        this.projectGroupRepository.addProjectCountById(defaultGroupId, 1);
    }

    @Transactional
    public void updateProjectSort(String projectGroupId, Integer originSort, Integer targetSort) {
        var linkGroups = this.projectLinkGroupRepository.findAllByGroupIdAndSortBetween(projectGroupId,
                Math.min(originSort, targetSort),
                Math.max(originSort, targetSort));
        if (linkGroups.isEmpty()) {
            return;
        }
        // 删除分组中间表
        this.projectLinkGroupRepository.deleteByIdIn(linkGroups.stream().map(ProjectLinkGroup::getId).collect(Collectors.toList()));
        // 添加分组中间表
        var newLinkGroups = new ArrayList<ProjectLinkGroup>();
        if (originSort > targetSort) {
            for (int i = 0; i < linkGroups.size() - 1; i++) {
                newLinkGroups.add(ProjectLinkGroup.Builder.aReference()
                        .projectGroupId(projectGroupId)
                        .projectId(linkGroups.get(i).getProjectId())
                        .sort(linkGroups.get(i + 1).getSort())
                        .build());
            }
        } else {
            for (int i = 1; i < linkGroups.size(); i++) {
                newLinkGroups.add(ProjectLinkGroup.Builder.aReference()
                        .projectGroupId(projectGroupId)
                        .projectId(linkGroups.get(i).getProjectId())
                        .sort(linkGroups.get(i - 1).getSort())
                        .build());
            }

        }
        newLinkGroups.add(ProjectLinkGroup.Builder.aReference()
                .projectGroupId(projectGroupId)
                .projectId(originSort > targetSort ? linkGroups.get(linkGroups.size() - 1).getProjectId() : linkGroups.get(0).getProjectId())
                .sort(targetSort)
                .build());
        this.projectLinkGroupRepository.addAll(newLinkGroups);
    }

    public PageInfo<ProjectLinkGroup> findLinkPageByGroupId(Integer pageNum, Integer pageSize, String projectGroupId) {
        return this.projectLinkGroupRepository.findPageByGroupId(pageNum, pageSize, projectGroupId);
    }

    public Optional<ProjectLinkGroup> findLinkByProjectId(String id) {
        return this.projectLinkGroupRepository.findByProjectId(id);
    }

    public List<ProjectLinkGroup> findLinkByProjectIdIn(List<String> projectIds) {
        return this.projectLinkGroupRepository.findAllByProjectIdIn(projectIds);
    }
}
