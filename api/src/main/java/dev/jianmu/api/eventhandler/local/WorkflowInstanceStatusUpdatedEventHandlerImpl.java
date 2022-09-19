package dev.jianmu.api.eventhandler.local;

import dev.jianmu.api.eventhandler.WorkflowInstanceStatusUpdatedEventHandler;
import dev.jianmu.event.impl.WorkflowInstanceStatusUpdatedEvent;
import dev.jianmu.infrastructure.sse.WorkflowInstanceStatusSubscribeService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "jianmu.event", name = "type", havingValue = "local", matchIfMissing = true)
public class WorkflowInstanceStatusUpdatedEventHandlerImpl extends WorkflowInstanceStatusUpdatedEventHandler {
    public WorkflowInstanceStatusUpdatedEventHandlerImpl(WorkflowInstanceStatusSubscribeService workflowInstanceStatusSubscribeService) {
        super(workflowInstanceStatusSubscribeService);
    }
    @Async
    @EventListener
    @Override
    public void subscribe(WorkflowInstanceStatusUpdatedEvent event) {
        super.subscribe(event);
    }
}
