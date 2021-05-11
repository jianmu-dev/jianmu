package dev.jianmu.api.controller;

import com.github.pagehelper.PageInfo;
import dev.jianmu.api.dto.DslTextDto;
import dev.jianmu.api.dto.ProjectSearchDto;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.service.DslApplication;
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

    public DslController(DslApplication dslApplication) {
        this.dslApplication = dslApplication;
    }

    @PostMapping("/trigger/{dslId}")
    @Operation(summary = "DSL触发接口", description = "触发DSL启动")
    public void trigger(@Parameter(description = "触发器ID") @PathVariable String dslId) {
        this.dslApplication.trigger(dslId);
    }

    @PutMapping("project/{dslId}")
    @Operation(summary = "更新项目", description = "更新项目DSL定义")
    public void updateProject(@PathVariable String dslId, @RequestBody @Valid DslTextDto dslTextDto) {
        this.dslApplication.updateProject(dslId, dslTextDto.getDslText());
    }

    @GetMapping("project/{dslId}")
    @Operation(summary = "获取项目详情", description = "获取项目详情")
    public Project getProject(@PathVariable String dslId) {
        return this.dslApplication.findById(dslId).orElseThrow(() -> new DataNotFoundException("未找到该项目"));
    }

    @PostMapping("/project")
    @Operation(summary = "创建项目", description = "上传DSL并创建项目")
    public void createProject(@RequestBody @Valid DslTextDto dslTextDto) {
        this.dslApplication.createProject(dslTextDto.getDslText());
    }

    @DeleteMapping("/{dslId}")
    @Operation(summary = "删除DSL定义", description = "删除DSL定义")
    public void deleteById(@PathVariable String dslId) {
        this.dslApplication.deleteById(dslId);
    }

    @GetMapping("/source/{ref}/{version}")
    public DslSourceCode findByRefAndVersion(@PathVariable String ref, @PathVariable String version) {
        return this.dslApplication.findByRefAndVersion(ref, version);
    }

    @GetMapping
    @Operation(summary = "分页查询DSL列表", description = "分页查询DSL列表")
    public PageInfo<Project> findAll(ProjectSearchDto searchDto) {
        return this.dslApplication.findAll(searchDto.getName(), searchDto.getPageNum(), searchDto.getPageSize());
    }
}
