package dev.jianmu.api.controller;

import com.github.pagehelper.PageInfo;
import dev.jianmu.api.dto.WorkflowInstanceSearchDto;
import dev.jianmu.api.mapper.WorkflowInstanceMapper;
import dev.jianmu.api.vo.PageUtils;
import dev.jianmu.api.vo.WorkflowInstanceVo;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.service.WorkflowInstanceApplication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * @class: WorkflowInstanceController
 * @description: 流程实例接口类
 * @author: Ethan Liu
 * @create: 2021-03-24 16:02
 **/
@RestController
@RequestMapping("workflow_instances")
@Tag(name = "流程实例接口", description = "提供流程实例启动停止等API")
@SecurityRequirement(name = "bearerAuth")
public class WorkflowInstanceController {
    private final WorkflowInstanceApplication instanceApplication;

    public WorkflowInstanceController(WorkflowInstanceApplication instanceApplication) {
        this.instanceApplication = instanceApplication;
    }

    @GetMapping
    public PageInfo<WorkflowInstanceVo> findAll(WorkflowInstanceSearchDto searchDto) {
        var page = this.instanceApplication.findAllPage(
                searchDto.getId(),
                searchDto.getName(),
                searchDto.getWorkflowVersion(),
                searchDto.getStatus(),
                searchDto.getPageNum(),
                searchDto.getPageSize()
        );
        var instances = page.getList();
        var newInstances = WorkflowInstanceMapper.INSTANCE.toWorkflowInstanceVoList(instances);
        PageInfo<WorkflowInstanceVo> newPage = PageUtils.pageInfo2PageInfoVo(page);
        newPage.setList(newInstances);
        return newPage;
    }

    @GetMapping("/{id}")
    @Operation(summary = "流程实例详情", description = "流程实例详情")
    public WorkflowInstanceVo findById(@PathVariable String id) {
        var instance = this.instanceApplication.findById(id)
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        return WorkflowInstanceMapper.INSTANCE.toWorkflowInstanceVo(instance);
    }

    @PutMapping("/{instanceId}/{nodeRef}")
    @Operation(summary = "流程启动", description = "流程启动")
    public void start(
            @Parameter(description = "流程实例ID") @PathVariable String instanceId,
            @Parameter(description = "启动节点定义名") @PathVariable String nodeRef
    ) {
        this.instanceApplication.start(instanceId, nodeRef);
    }

    @PutMapping("/stop/{instanceId}")
    @Operation(summary = "流程结束接口", description = "流程结束接口")
    public void stop(
            @Parameter(description = "流程实例ID") @PathVariable String instanceId
    ) {
        this.instanceApplication.stop(instanceId);
    }
}
