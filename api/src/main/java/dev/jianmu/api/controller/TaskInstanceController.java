package dev.jianmu.api.controller;

import dev.jianmu.api.mapper.TaskInstanceMapper;
import dev.jianmu.api.vo.TaskInstanceVo;
import dev.jianmu.application.service.TaskInstanceApplication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @class: TaskInstanceController
 * @description: 任务实例接口
 * @author: Ethan Liu
 * @create: 2021-04-22 12:27
 **/
@RestController
@RequestMapping("task_instances")
@Tag(name = "任务实例接口", description = "任务实例接口")
public class TaskInstanceController {
    private final TaskInstanceApplication taskInstanceApplication;

    public TaskInstanceController(TaskInstanceApplication taskInstanceApplication) {
        this.taskInstanceApplication = taskInstanceApplication;
    }

    @GetMapping("/{workflowInstanceId}")
    @Operation(summary = "任务实例列表接口", description = "任务实例列表接口")
    public List<TaskInstanceVo> findByBusinessId(@PathVariable String workflowInstanceId) {
        List<TaskInstanceVo> list = new ArrayList<>();
        var taskInstances = this.taskInstanceApplication.findByBusinessId(workflowInstanceId);
        taskInstances.forEach(taskInstance -> {
            var version = this.taskInstanceApplication
                    .findByDefKey(taskInstance.getDefKey()).orElseThrow(() -> new RuntimeException("未知任务版本"));
            var definition = this.taskInstanceApplication
                    .findByRef(version.getTaskDefinitionRef()).orElseThrow(() -> new RuntimeException("未知定义定义"));
            var vo = TaskInstanceMapper.INSTANCE.toTaskInstanceVo(taskInstance, definition, version);
            list.add(vo);
        });
        return list;
    }
}
