package dev.jianmu.api.controller;

import dev.jianmu.api.dto.AddGroup;
import dev.jianmu.api.dto.DslTextDto;
import dev.jianmu.api.dto.GitRepoDto;
import dev.jianmu.api.mapper.GitRepoMapper;
import dev.jianmu.application.service.GitApplication;
import dev.jianmu.application.service.ProjectApplication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @class: ProjectController
 * @description: ProjectController
 * @author: Ethan Liu
 * @create: 2021-05-14 14:00
 **/
@RestController
@RequestMapping("projects")
@Tag(name = "项目API", description = "项目API")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {
    private final ProjectApplication projectApplication;
    private final GitApplication gitApplication;

    public ProjectController(ProjectApplication projectApplication, GitApplication gitApplication) {
        this.projectApplication = projectApplication;
        this.gitApplication = gitApplication;
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
    @Operation(summary = "更新项目", description = "更新项目DSL定义")
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
}
