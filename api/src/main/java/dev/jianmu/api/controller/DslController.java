package dev.jianmu.api.controller;

import com.github.pagehelper.PageInfo;
import dev.jianmu.api.dto.DslTextDto;
import dev.jianmu.api.dto.ProjectSearchDto;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.service.DslApplication;
import dev.jianmu.application.service.ProjectApplication;
import dev.jianmu.project.aggregate.DslSourceCode;
import dev.jianmu.project.aggregate.Project;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @class: DslController
 * @description: DSL API
 * @author: Ethan Liu
 * @create: 2021-04-20 20:03
 **/
@RestController
@RequestMapping("dsl")
@Tag(name = "DSL", description = "DSL API")
public class DslController {
    private final DslApplication dslApplication;
    private final ProjectApplication projectApplication;

    public DslController(DslApplication dslApplication, ProjectApplication projectApplication) {
        this.dslApplication = dslApplication;
        this.projectApplication = projectApplication;
    }

    @PostMapping("/trigger/{dslId}")
    @Operation(summary = "DSL触发接口", description = "触发DSL启动", deprecated = true)
    public void trigger(@Parameter(description = "触发器ID") @PathVariable String dslId) {
        this.projectApplication.trigger(dslId);
    }

    @PutMapping("project/{dslId}")
    @Operation(summary = "更新项目", description = "更新项目DSL定义", deprecated = true)
    public void updateProject(@PathVariable String dslId, @RequestBody @Valid DslTextDto dslTextDto) {
        this.dslApplication.updateProject(dslId, dslTextDto.getDslText());
    }

    @GetMapping("project/{dslId}")
    @Operation(summary = "获取项目详情", description = "获取项目详情", deprecated = true)
    public Project getProject(@PathVariable String dslId) {
        return this.projectApplication.findById(dslId).orElseThrow(() -> new DataNotFoundException("未找到该项目"));
    }

    @PostMapping("/project")
    @Operation(summary = "创建项目", description = "上传DSL并创建项目", deprecated = true)
    public void createProject(@RequestBody @Valid DslTextDto dslTextDto) {
        this.dslApplication.createProject(dslTextDto.getDslText());
    }

    @DeleteMapping("/{dslId}")
    @Operation(summary = "删除DSL定义", description = "删除DSL定义", deprecated = true)
    public void deleteById(@PathVariable String dslId) {
        this.projectApplication.deleteById(dslId);
    }

    @GetMapping("/source/{ref}/{version}")
    @Operation(deprecated = true)
    public DslSourceCode findByRefAndVersion(@PathVariable String ref, @PathVariable String version) {
        return this.projectApplication.findByRefAndVersion(ref, version);
    }

    @GetMapping
    @Operation(summary = "分页查询DSL列表", description = "分页查询DSL列表", deprecated = true)
    public PageInfo<Project> findAll(ProjectSearchDto searchDto) {
        return this.projectApplication.findAll(searchDto.getName(), searchDto.getPageNum(), searchDto.getPageSize());
    }
}
