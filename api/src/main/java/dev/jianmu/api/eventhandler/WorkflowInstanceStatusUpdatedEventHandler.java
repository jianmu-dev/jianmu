package dev.jianmu.api.eventhandler;

import dev.jianmu.event.Subscriber;
import dev.jianmu.event.impl.WorkflowInstanceStatusUpdatedEvent;
import dev.jianmu.infrastructure.sse.WorkflowInstanceStatusSubscribeService;
import dev.jianmu.workflow.aggregate.process.ProcessStatus;

/**
 * @author Daihw
 * @class WorkflowInstanceStatusUpdatedEventHandler
 * @description 流程实例状态变更处理器
 * @create 2022/9/16 9:40 上午
 */
public class WorkflowInstanceStatusUpdatedEventHandler implements Subscriber<WorkflowInstanceStatusUpdatedEvent> {
    private final WorkflowInstanceStatusSubscribeService workflowInstanceStatusSubscribeService;

    public WorkflowInstanceStatusUpdatedEventHandler(WorkflowInstanceStatusSubscribeService workflowInstanceStatusSubscribeService) {
        this.workflowInstanceStatusSubscribeService = workflowInstanceStatusSubscribeService;
    }

    @Override
    public void subscribe(WorkflowInstanceStatusUpdatedEvent event) {
        this.workflowInstanceStatusSubscribeService.sendMessageByWorkflowRef(event.getWorkflowRef(), event);
    }
}
