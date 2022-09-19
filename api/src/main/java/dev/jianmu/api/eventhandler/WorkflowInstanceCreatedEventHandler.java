package dev.jianmu.api.eventhandler;

import dev.jianmu.event.Subscriber;
import dev.jianmu.event.impl.WorkflowInstanceCreatedEvent;
import dev.jianmu.event.impl.WorkflowInstanceStatusUpdatedEvent;
import dev.jianmu.infrastructure.sse.WorkflowInstanceStatusSubscribeService;
import dev.jianmu.workflow.aggregate.process.ProcessStatus;

/**
 * @author Daihw
 * @class WorkflowInstanceCreatedEventHandler
 * @description 流程实例状态变更处理器
 * @create 2022/9/16 9:40 上午
 */
public class WorkflowInstanceCreatedEventHandler implements Subscriber<WorkflowInstanceCreatedEvent> {
    private final WorkflowInstanceStatusSubscribeService workflowInstanceStatusSubscribeService;

    public WorkflowInstanceCreatedEventHandler(WorkflowInstanceStatusSubscribeService workflowInstanceStatusSubscribeService) {
        this.workflowInstanceStatusSubscribeService = workflowInstanceStatusSubscribeService;
    }

    @Override
    public void subscribe(WorkflowInstanceCreatedEvent event) {
        this.workflowInstanceStatusSubscribeService.sendWorkflowInstanceMessage(event.getWorkflowRef(), event);
    }
}
