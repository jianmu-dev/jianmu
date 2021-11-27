package dev.jianmu.api.controller;

import dev.jianmu.api.dto.*;
import dev.jianmu.api.mapper.GitRepoMapper;
import dev.jianmu.api.mapper.ProjectGroupDtoMapper;
import dev.jianmu.application.service.GitApplication;
import dev.jianmu.application.service.ProjectApplication;
import dev.jianmu.application.service.ProjectGroupApplication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @class ProjectController
 * @description ProjectController
 * @author Ethan Liu
 * @create 2021-05-14 14:00
*/
@RestController
@RequestMapping("projects")
@Tag(name = "项目API", description = "项目API")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {
    private final ProjectApplication projectApplication;
    private final GitApplication gitApplication;
    private final ProjectGroupApplication projectGroupApplication;

    public ProjectController(ProjectApplication projectApplication,
                             GitApplication gitApplication,
                             ProjectGroupApplication projectGroupApplication) {
        this.projectApplication = projectApplication;
        this.gitApplication = gitApplication;
        this.projectGroupApplication = projectGroupApplication;
    }

    @PostMapping("/trigger/{projectId}")
    @Operation(summary = "触发项目", description = "触发项目启动")
    public void trigger(@Parameter(description = "触发器ID") @PathVariable String projectId) {
        this.projectApplication.triggerByManual(projectId);
    }

    @PostMapping
    @Operation(summary = "创建项目", description = "上传DSL并创建项目")
    public void createProject(@RequestBody @Valid DslTextDto dslTextDto) {
        this.projectApplication.createProject(dslTextDto.getDslText());
    }

    @PostMapping("/import")
    @Operation(summary = "导入DSL", description = "导入Git库中的DSL文件创建项目")
    public void importDsl(@RequestBody @Validated(AddGroup.class) GitRepoDto gitRepoDto) {
        var gitRepo = GitRepoMapper.INSTANCE.toGitRepo(gitRepoDto);
        this.projectApplication.importProject(gitRepo);
    }

    @PutMapping("/{projectId}")
    @Operation(summary = "更新项目", description = "根据ID更新项目DSL定义")
    public void updateProject(@PathVariable String projectId, @RequestBody @Valid DslTextDto dslTextDto) {
        this.projectApplication.updateProject(projectId, dslTextDto.getDslText());
    }

    @PutMapping("/sync/{projectId}")
    @Operation(summary = "同步DSL", description = "同步Git库中的DSL文件更新项目")
    public void syncProject(@PathVariable String projectId) {
        this.gitApplication.syncGitRepo(projectId);
    }

    @DeleteMapping("/{projectId}")
    @Operation(summary = "删除项目", description = "删除项目")
    public void deleteById(@PathVariable String projectId) {
        this.projectApplication.deleteById(projectId);
    }

    @PostMapping("/groups")
    @Operation(summary = "创建项目组", description = "创建项目组")
    public void createProjectGroup(@RequestBody @Valid ProjectGroupDto projectGroupDto) {
        var projectGroup = ProjectGroupDtoMapper.INSTANCE.toProjectGroup(projectGroupDto);
        this.projectGroupApplication.createProjectGroup(projectGroup);
    }

    @PutMapping("/groups/{projectGroupId}")
    @Operation(summary = "编辑项目组", description = "编辑项目组")
    public void updateProjectGroup(@PathVariable String projectGroupId, @RequestBody @Valid ProjectGroupDto projectGroupDto) {
        var projectGroup = ProjectGroupDtoMapper.INSTANCE.toProjectGroup(projectGroupDto);
        this.projectGroupApplication.updateProjectGroup(projectGroupId, projectGroup);
    }

    @DeleteMapping("/groups/{projectGroupId}")
    @Operation(summary = "删除项目组", description = "删除项目组")
    public void deleteProjectGroup(@PathVariable String projectGroupId) {
        this.projectGroupApplication.deleteById(projectGroupId);
    }

    @PutMapping("/groups/sort")
    @Operation(summary = "修改项目组排序", description = "修改项目组排序")
    public void updateProjectGroupSort(@RequestBody @Valid ProjectGroupSortUpdatingDto projectGroupSortUpdatingDto) {
        this.projectGroupApplication.updateSort(projectGroupSortUpdatingDto.getOriginSort(), projectGroupSortUpdatingDto.getTargetSort());
    }

    @PostMapping("/groups/projects")
    @Operation(summary = "项目组添加项目", description = "项目组添加项目")
    public void addProjectByGroupId(@RequestBody @Valid ProjectGroupAddingDto projectGroupAddingDto) {
        this.projectGroupApplication.addProject(projectGroupAddingDto.getProjectGroupId(), projectGroupAddingDto.getProjectIds());
    }

    @DeleteMapping("/groups/projects/{projectLinkGroupId}")
    @Operation(summary = "项目组删除项目", description = "项目组删除项目")
    public void deleteProjectByGroup(@PathVariable String projectLinkGroupId) {
        this.projectGroupApplication.deleteProject(projectLinkGroupId);
    }

    @PostMapping("/groups/{projectGroupId}/projects/sort")
    @Operation(summary = "修改项目组中的项目排序", description = "修改项目组中的项目排序")
    public void updateProjectSort(@PathVariable String projectGroupId, @RequestBody @Valid ProjectSortUpdatingDto projectSortUpdatingDto) {
        this.projectGroupApplication.updateProjectSort(projectGroupId, projectSortUpdatingDto.getOriginSort(), projectSortUpdatingDto.getTargetSort());
    }
}
