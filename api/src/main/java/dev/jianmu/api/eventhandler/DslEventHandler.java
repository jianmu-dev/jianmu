package dev.jianmu.api.eventhandler;

import dev.jianmu.application.service.WorkflowInstanceApplication;
import dev.jianmu.project.aggregate.Project;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @class: DslEventHandler
 * @description: Dsl事件处理器
 * @author: Ethan Liu
 * @create: 2021-04-23 17:21
 **/
@Component
public class DslEventHandler {
    private final WorkflowInstanceApplication workflowInstanceApplication;

    public DslEventHandler(WorkflowInstanceApplication workflowInstanceApplication) {
        this.workflowInstanceApplication = workflowInstanceApplication;
    }

    @Async
    @EventListener
    public void handleTriggerEvent(Project project) {
        // 使用project id与WorkflowVersion作为triggerId,用于参数引用查询，参见WorkerApplication#getEnvironmentMap
        this.workflowInstanceApplication.createAndStart(
                project.getId() + project.getWorkflowVersion(),
                project.getWorkflowRef() + project.getWorkflowVersion()
        );
    }
}
