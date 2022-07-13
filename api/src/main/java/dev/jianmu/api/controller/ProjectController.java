package dev.jianmu.api.controller;

import dev.jianmu.api.dto.DslTextDto;
import dev.jianmu.api.vo.ProjectIdVo;
import dev.jianmu.application.service.ProjectApplication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author Ethan Liu
 * @class ProjectController
 * @description ProjectController
 * @create 2021-05-14 14:00
 */
@RestController
@RequestMapping("projects")
@Tag(name = "项目API", description = "项目API")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {
    private final ProjectApplication projectApplication;

    public ProjectController(ProjectApplication projectApplication) {
        this.projectApplication = projectApplication;
    }

    @PutMapping("/enable/{projectId}")
    @Operation(summary = "激活项目", description = "激活项目")
    public void enable(@PathVariable String projectId) {
        this.projectApplication.switchEnabled(projectId, true);
    }

    @PutMapping("/disable/{projectId}")
    @Operation(summary = "禁用项目", description = "禁用项目")
    public void disable(@PathVariable String projectId) {
        this.projectApplication.switchEnabled(projectId, false);
    }

    @PostMapping("/trigger/{projectId}")
    @Operation(summary = "触发项目", description = "触发项目启动")
    public void trigger(@Parameter(description = "触发器ID") @PathVariable String projectId) {
        this.projectApplication.triggerByManual(projectId);
    }

    @PostMapping
    @Operation(summary = "创建项目", description = "上传DSL并创建项目")
    public ProjectIdVo createProject(@RequestBody @Valid DslTextDto dslTextDto) {
        var project = this.projectApplication.createProject(dslTextDto.getDslText(), dslTextDto.getProjectGroupId(), dslTextDto.getBranch());
        return ProjectIdVo.builder().id(project.getId()).build();
    }

    @PutMapping("/{projectId}")
    @Operation(summary = "更新项目", description = "根据ID更新项目DSL定义")
    public void updateProject(@PathVariable String projectId, @RequestBody @Valid DslTextDto dslTextDto) {
        this.projectApplication.updateProject(projectId, dslTextDto.getDslText(), dslTextDto.getProjectGroupId());
    }

    @DeleteMapping("/{projectId}")
    @Operation(summary = "删除项目", description = "删除项目")
    public void deleteById(@PathVariable String projectId) {
        this.projectApplication.deleteById(projectId);
    }
}
