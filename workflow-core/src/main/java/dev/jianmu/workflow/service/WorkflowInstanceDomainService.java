package dev.jianmu.workflow.service;

import dev.jianmu.workflow.aggregate.definition.Workflow;
import dev.jianmu.workflow.aggregate.process.WorkflowInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ethan Liu
 * @program: workflow
 * @description 流程实例DomainService
 * @create 2021-01-21 21:16
 */
public class WorkflowInstanceDomainService {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowInstanceDomainService.class);

    public WorkflowInstance create(String triggerId, String triggerType, int serialNo, Workflow workflow) {
        // 构造流程实例
        return WorkflowInstance.Builder.aWorkflowInstance()
                .serialNo(serialNo)
                .triggerId(triggerId)
                .triggerType(triggerType)
                .name(workflow.getName())
                .description(workflow.getDescription())
                .workflowRef(workflow.getRef())
                .workflowVersion(workflow.getVersion())
                .build();
    }
}
