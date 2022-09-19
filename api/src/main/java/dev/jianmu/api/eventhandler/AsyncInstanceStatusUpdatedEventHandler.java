package dev.jianmu.api.eventhandler;

import dev.jianmu.event.Subscriber;
import dev.jianmu.event.impl.AsyncTaskInstanceStatusUpdatedEvent;
import dev.jianmu.infrastructure.sse.WorkflowInstanceStatusSubscribeService;

/**
 * @class AsyncInstanceStatusUpdatedEventHandler
 * @description 异步任务实例状态变更处理器
 * @author Daihw
 * @create 2022/9/16 9:43 上午
 */
public class AsyncInstanceStatusUpdatedEventHandler implements Subscriber<AsyncTaskInstanceStatusUpdatedEvent> {
    private final WorkflowInstanceStatusSubscribeService workflowInstanceStatusSubscribeService;

    public AsyncInstanceStatusUpdatedEventHandler(WorkflowInstanceStatusSubscribeService workflowInstanceStatusSubscribeService) {
        this.workflowInstanceStatusSubscribeService = workflowInstanceStatusSubscribeService;
    }

    @Override
    public void subscribe(AsyncTaskInstanceStatusUpdatedEvent event) {
        this.workflowInstanceStatusSubscribeService.sendWorkflowInstanceMessage(event.getWorkflowRef(), event.getWorkflowInstanceId(), event);
    }
}
