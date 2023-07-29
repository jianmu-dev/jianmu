package dev.jianmu.api.controller;

import dev.jianmu.api.dto.ProjectGroupAddingDto;
import dev.jianmu.api.dto.ProjectGroupDto;
import dev.jianmu.api.dto.ProjectGroupSortUpdatingDto;
import dev.jianmu.api.dto.ProjectSortUpdatingDto;
import dev.jianmu.api.mapper.ProjectGroupDtoMapper;
import dev.jianmu.application.service.ProjectGroupApplication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * @class ProjectGroupController
 * @description 项目组API
 * @author Daihw
 * @create 2021/11/29 6:10 下午
 */
@RestController
@RequestMapping("projects/groups")
@Tag(name = "项目组API", description = "项目组API")
@SecurityRequirement(name = "bearerAuth")
public class ProjectGroupController {

    private final ProjectGroupApplication projectGroupApplication;

    public ProjectGroupController(ProjectGroupApplication projectGroupApplication) {
        this.projectGroupApplication = projectGroupApplication;
    }

    @PostMapping
    @Operation(summary = "创建项目组", description = "创建项目组")
    public void createProjectGroup(@RequestBody @Valid ProjectGroupDto projectGroupDto) {
        var projectGroup = ProjectGroupDtoMapper.INSTANCE.toProjectGroup(projectGroupDto);
        this.projectGroupApplication.createProjectGroup(projectGroup);
    }

    @PutMapping("/{projectGroupId}")
    @Operation(summary = "编辑项目组", description = "编辑项目组")
    public void updateProjectGroup(@PathVariable String projectGroupId, @RequestBody @Valid ProjectGroupDto projectGroupDto) {
        var projectGroup = ProjectGroupDtoMapper.INSTANCE.toProjectGroup(projectGroupDto);
        this.projectGroupApplication.updateProjectGroup(projectGroupId, projectGroup);
    }

    @DeleteMapping("/{projectGroupId}")
    @Operation(summary = "删除项目组", description = "删除项目组")
    public void deleteProjectGroup(@PathVariable String projectGroupId) {
        this.projectGroupApplication.deleteById(projectGroupId);
    }

    @PatchMapping("/sort")
    @Operation(summary = "修改项目组排序", description = "修改项目组排序")
    public void updateProjectGroupSort(@RequestBody @Valid ProjectGroupSortUpdatingDto projectGroupSortUpdatingDto) {
        this.projectGroupApplication.updateSort(projectGroupSortUpdatingDto.getOriginGroupId(), projectGroupSortUpdatingDto.getTargetGroupId());
    }

    @PutMapping("/{projectGroupId}/is_show")
    @Operation(summary = "修改项目组是否展示", description = "修改项目组是否展示")
    public void updateProjectGroupIsShow(@PathVariable String projectGroupId) {
        this.projectGroupApplication.updateProjectGroupIsShow(projectGroupId);
    }

    @PostMapping("/projects")
    @Operation(summary = "项目组添加项目", description = "项目组添加项目")
    public void addProjectByGroupId(@RequestBody @Valid ProjectGroupAddingDto projectGroupAddingDto) {
        this.projectGroupApplication.addProject(projectGroupAddingDto.getProjectGroupId(), projectGroupAddingDto.getProjectIds());
    }

    @DeleteMapping("/projects/{projectId}")
    @Operation(summary = "项目组删除项目", description = "项目组删除项目")
    public void deleteProjectByGroup(@PathVariable String projectId) {
        this.projectGroupApplication.deleteProject(projectId);
    }

    @PatchMapping("/{projectGroupId}/projects/sort")
    @Operation(summary = "修改项目组中的项目排序", description = "修改项目组中的项目排序")
    public void updateProjectSort(@PathVariable String projectGroupId, @RequestBody @Valid ProjectSortUpdatingDto projectSortUpdatingDto) {
        this.projectGroupApplication.updateProjectSort(projectGroupId, projectSortUpdatingDto.getOriginProjectId(), projectSortUpdatingDto.getTargetProjectId());
    }
}
