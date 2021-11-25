package dev.jianmu.api.controller;

import dev.jianmu.application.service.internal.WorkflowInstanceInternalApplication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @class WorkflowInstanceController
 * @description 流程实例接口类
 * @author Ethan Liu
 * @create 2021-03-24 16:02
*/
@RestController
@RequestMapping("workflow_instances")
@Tag(name = "流程实例接口", description = "提供流程实例启动停止等API")
@SecurityRequirement(name = "bearerAuth")
public class WorkflowInstanceController {
    private final WorkflowInstanceInternalApplication instanceApplication;

    public WorkflowInstanceController(WorkflowInstanceInternalApplication instanceApplication) {
        this.instanceApplication = instanceApplication;
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
