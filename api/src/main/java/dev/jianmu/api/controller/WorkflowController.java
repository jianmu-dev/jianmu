package dev.jianmu.api.controller;

import dev.jianmu.application.service.WorkflowApplication;
import dev.jianmu.api.grpc.WorkerStreamServiceImpl;
import dev.jianmu.workflow.aggregate.definition.Workflow;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @class: WorkflowController
 * @description: 流程接口类
 * @author: Ethan Liu
 * @create: 2021-02-12 16:37
 **/
@RestController
@RequestMapping("workflows")
public class WorkflowController {

    @Resource
    private WorkflowApplication workflowApplication;

    @Resource
    private WorkerStreamServiceImpl workerStreamService;

    @PostMapping
    public Workflow create(@RequestBody Workflow workflow) {
        return this.workflowApplication.create(workflow);
    }

    @DeleteMapping
    public void delete(@RequestParam String ref, String version) {
        this.workflowApplication.deleteByRefAndVersion(ref, version);
    }

    @GetMapping
    public List<Workflow> find(
            @RequestParam(value = "ref") String ref,
            @RequestParam(value = "version", required = false) String version
    ) {
        if (null == version) {
            return this.workflowApplication.findByRef(ref);
        } else {
            List<Workflow> ws = new ArrayList<>();
            this.workflowApplication.findByRefAndVersion(ref, version)
                    .ifPresent(ws::add);
            return ws;
        }
    }
}
