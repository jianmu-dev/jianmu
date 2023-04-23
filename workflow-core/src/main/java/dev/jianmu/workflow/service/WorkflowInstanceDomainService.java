package dev.jianmu.workflow.service;

import dev.jianmu.workflow.aggregate.definition.GlobalParameter;
import dev.jianmu.workflow.aggregate.definition.Workflow;
import dev.jianmu.workflow.aggregate.process.AsyncTaskInstance;
import dev.jianmu.workflow.aggregate.process.TaskStatus;
import dev.jianmu.workflow.aggregate.process.WorkflowInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * @author Ethan Liu
 * @program: workflow
 * @description 流程实例DomainService
 * @create 2021-01-21 21:16
 */
public class WorkflowInstanceDomainService {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowInstanceDomainService.class);

    public WorkflowInstance create(String triggerId, String triggerType, int serialNo, Workflow workflow, Set<GlobalParameter> globalParameters) {
        // 构造流程实例
        return WorkflowInstance.Builder.aWorkflowInstance()
                .serialNo(serialNo)
                .triggerId(triggerId)
                .triggerType(triggerType)
                .name(workflow.getName())
                .description(workflow.getDescription())
                .workflowRef(workflow.getRef())
                .workflowVersion(workflow.getVersion())
                .globalParameters(globalParameters)
                .build();
    }

    public boolean canResume(List<AsyncTaskInstance> asyncTaskInstances, String taskRef) {
        // 除了当前任务之外，没有失败的任务时可以恢复
        var c = asyncTaskInstances.stream()
                .filter(t -> !t.getAsyncTaskRef().equals(taskRef))
                .filter(t -> t.getStatus() != TaskStatus.SUSPENDED)
                .filter(t -> t.getStatus() != TaskStatus.FAILED)
                .count();
        return c != 0;
    }
}
