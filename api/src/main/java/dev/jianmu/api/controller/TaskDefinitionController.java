package dev.jianmu.api.controller;

import com.github.pagehelper.PageInfo;
import dev.jianmu.api.dto.PageDto;
import dev.jianmu.api.dto.TaskDefinitionDto;
import dev.jianmu.api.dto.TaskDefinitionVersionDto;
import dev.jianmu.api.mapper.ContainerSpecMapper;
import dev.jianmu.api.mapper.TaskDefinitionDtoMapper;
import dev.jianmu.application.service.TaskDefinitionApplication;
import dev.jianmu.task.aggregate.DockerDefinition;
import dev.jianmu.version.aggregate.TaskDefinition;
import dev.jianmu.version.aggregate.TaskDefinitionVersion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.List;

/**
 * @class: TaskDefinitionController
 * @description: 任务定义接口
 * @author: Ethan Liu
 * @create: 2021-04-12 11:44
 **/
@RestController
@RequestMapping("task_definitions")
@Tag(name = "任务定义接口", description = "提供任务定义创建删除等API")
public class TaskDefinitionController {
    private final TaskDefinitionApplication taskDefinitionApplication;

    @Inject
    public TaskDefinitionController(TaskDefinitionApplication taskDefinitionApplication) {
        this.taskDefinitionApplication = taskDefinitionApplication;
    }

    @PostMapping
    @Operation(summary = "创建任务定义", description = "创建任务定义")
    public void create(@RequestBody @Valid TaskDefinitionDto dto) {
        var spec = ContainerSpecMapper.INSTANCE.toContainerSpec(dto.getSpec());
        var taskDefinition = TaskDefinitionDtoMapper.INSTANCE.toTaskDefinition(dto);
        var taskDefinitionVersion = TaskDefinitionDtoMapper.INSTANCE.toTaskDefinitionVersion(dto);
        this.taskDefinitionApplication.createDockerDefinition(
                taskDefinition,
                taskDefinitionVersion,
                dto.getResultFile(),
                dto.getInputParameters(),
                dto.getOutputParameters(),
                spec
        );
    }

    @PostMapping("/versions")
    @Operation(summary = "创建任务定义版本", description = "创建任务定义版本")
    public void createVersion(@RequestBody @Valid TaskDefinitionVersionDto dto) {
        var spec = ContainerSpecMapper.INSTANCE.toContainerSpec(dto.getSpec());
        var taskDefinitionVersion = TaskDefinitionDtoMapper.INSTANCE.toTaskDefinitionVersion(dto);
        this.taskDefinitionApplication.createDockerDefinitionVersion(
                taskDefinitionVersion,
                dto.getResultFile(),
                dto.getInputParameters(),
                dto.getOutputParameters(),
                spec
        );
    }

    @GetMapping
    @Operation(summary = "任务定义列表", description = "获取任务定义列表")
    public PageInfo<TaskDefinition> getTaskDefinitions(PageDto pageDto) {
        return this.taskDefinitionApplication.findAll(pageDto.getPageNum(), pageDto.getPageSize());
    }

    @GetMapping("/versions/{ref}")
    @Operation(summary = "获取任务定义版本", description = "获取任务定义版本")
    public List<TaskDefinitionVersion> getTaskDefinitionVersions(@PathVariable String ref) {
        return this.taskDefinitionApplication.findVersionByRef(ref);
    }

    @GetMapping("/versions/{ref}/{name}")
    @Operation(summary = "获取任务定义版本详情", description = "获取任务定义版本详情")
    public TaskDefinitionDto getDefinition(@PathVariable String ref, @PathVariable String name) {
        var taskDefinition = this.taskDefinitionApplication.findByRef(ref);
        var version = this.taskDefinitionApplication.findByRefAndName(ref, name);
        var definition = this.taskDefinitionApplication.findByKey(ref + name);
        return TaskDefinitionDtoMapper.INSTANCE.toTaskDefinitionDto((DockerDefinition) definition, version, taskDefinition);
    }

    @DeleteMapping("/versions/{ref}/{name}")
    @Operation(summary = "删除任务定义版本", description = "删除任务定义版本")
    public void deleteTaskDefinitionVersion(@PathVariable String ref, @PathVariable String name) {
        this.taskDefinitionApplication.deleteTaskDefinitionVersion(ref, name);
    }
}
