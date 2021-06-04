package dev.jianmu.api.controller;

import com.github.pagehelper.PageInfo;
import dev.jianmu.api.dto.ProjectSearchDto;
import dev.jianmu.api.mapper.TaskInstanceMapper;
import dev.jianmu.api.mapper.WorkflowInstanceMapper;
import dev.jianmu.api.vo.PageUtils;
import dev.jianmu.api.vo.ProjectVo;
import dev.jianmu.api.vo.TaskInstanceVo;
import dev.jianmu.api.vo.WorkflowInstanceVo;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.service.TaskInstanceApplication;
import dev.jianmu.application.service.WorkflowInstanceApplication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @class: ProjectViewController
 * @description: ProjectViewController
 * @author: Ethan Liu
 * @create: 2021-06-04 17:23
 **/
@RestController
@RequestMapping("view")
@Tag(name = "查询API", description = "查询API")
public class ViewController {
    private final WorkflowInstanceApplication instanceApplication;
    private final TaskInstanceApplication taskInstanceApplication;

    @Inject
    public ViewController(
            WorkflowInstanceApplication instanceApplication,
            TaskInstanceApplication taskInstanceApplication
    ) {
        this.instanceApplication = instanceApplication;
        this.taskInstanceApplication = taskInstanceApplication;
    }

    @GetMapping("/projects")
    @Operation(summary = "分页查询项目列表", description = "分页查询项目列表")
    public PageInfo<ProjectVo> findAll(ProjectSearchDto searchDto) {
        return PageInfo.of(List.of());
    }

    @GetMapping("/workflow_instances/{workflowRef}")
    @Operation(summary = "根据workflowRef查询流程实例列表", description = "根据workflowRef查询流程实例列表")
    public PageInfo<WorkflowInstanceVo> findByWorkflowRef(@PathVariable String workflowRef) {
        var page = this.instanceApplication.findByWorkflowRef(workflowRef);
        var instances = page.getList();
        var newInstances = WorkflowInstanceMapper.INSTANCE.toWorkflowInstanceVoList(instances);
        PageInfo<WorkflowInstanceVo> newPage = PageUtils.pageInfo2PageInfoVo(page);
        newPage.setList(newInstances);
        return newPage;
    }

    @GetMapping("/task_instances/{workflowInstanceId}")
    @Operation(summary = "任务实例列表接口", description = "任务实例列表接口")
    public List<TaskInstanceVo> findByBusinessId(@PathVariable String workflowInstanceId) {
        List<TaskInstanceVo> list = new ArrayList<>();
        var taskInstances = this.taskInstanceApplication.findByBusinessId(workflowInstanceId);
        taskInstances.forEach(taskInstance -> {
            var vo = TaskInstanceMapper.INSTANCE.toTaskInstanceVo(taskInstance);
            list.add(vo);
        });
        return list;
    }

    @GetMapping("/task_instance/{instanceId}")
    @Operation(summary = "任务实例详情接口", description = "任务实例详情接口")
    public TaskInstanceVo findById(@PathVariable String instanceId) {
        var taskInstance = this.taskInstanceApplication.findById(instanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该任务实例"));
        return TaskInstanceMapper.INSTANCE.toTaskInstanceVo(taskInstance);
    }
}
