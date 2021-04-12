package dev.jianmu.api.controller;

import dev.jianmu.application.service.WorkflowInstanceApplication;
import dev.jianmu.workflow.aggregate.process.WorkflowInstance;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

/**
 * @class: WorkflowInstanceController
 * @description: 流程实例接口类
 * @author: Ethan Liu
 * @create: 2021-03-24 16:02
 **/
@RestController
@RequestMapping("workflow_instances")
public class WorkflowInstanceController {
    private final WorkflowInstanceApplication instanceApplication;

    @Inject
    public WorkflowInstanceController(WorkflowInstanceApplication instanceApplication) {
        this.instanceApplication = instanceApplication;
    }

    @PutMapping("/{instanceId}/{nodeRef}")
    public WorkflowInstance start(@PathVariable String instanceId, @PathVariable String nodeRef) {
        return this.instanceApplication.start(instanceId, nodeRef);
    }

    @PutMapping("/stop")
    public void stop(String instanceId, String nodeRef) {
        this.instanceApplication.terminateNode(instanceId, nodeRef);
    }
}
