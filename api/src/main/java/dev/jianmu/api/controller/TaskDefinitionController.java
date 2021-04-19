package dev.jianmu.api.controller;

import com.github.pagehelper.PageInfo;
import dev.jianmu.api.dto.PageDto;
import dev.jianmu.api.dto.TaskDefinitionDto;
import dev.jianmu.api.dto.TaskDefinitionVersionDto;
import dev.jianmu.api.mapper.ContainerSpecMapper;
import dev.jianmu.application.service.TaskDefinitionApplication;
import dev.jianmu.version.aggregate.TaskDefinition;
import dev.jianmu.version.aggregate.TaskDefinitionVersion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
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
    public void create(TaskDefinitionDto dto) {
        var spec = ContainerSpecMapper.INSTANCE.toContainerSpec(dto.getSpec());
        this.taskDefinitionApplication.createDockerDefinition(
                dto.getName(), dto.getRef(), dto.getVersion(), dto.getResultFile(), dto.getDescription(), dto.getInputParameters(), spec
        );
    }

    @PostMapping("/versions")
    @Operation(summary = "创建任务定义版本", description = "创建任务定义版本")
    public void createVersion(TaskDefinitionVersionDto dto) {
        var spec = ContainerSpecMapper.INSTANCE.toContainerSpec(dto.getSpec());
        this.taskDefinitionApplication.createDockerDefinitionVersion(
                dto.getRef(), dto.getVersion(), dto.getResultFile(), dto.getDescription(), dto.getInputParameters(), spec
        );
    }

    @GetMapping
    public PageInfo<TaskDefinition> getTaskDefinitions(PageDto pageDto) {
        return this.taskDefinitionApplication.findAll(pageDto.getPageNum(), pageDto.getPageSize());
    }

    @GetMapping("/versions/{ref}")
    public List<TaskDefinitionVersion> getTaskDefinitionVersions(@PathVariable String ref) {
        return this.taskDefinitionApplication.findVersionByRef(ref);
    }
}
