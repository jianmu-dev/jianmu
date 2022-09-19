package dev.jianmu.api.eventhandler.local;

import dev.jianmu.api.eventhandler.WorkflowInstanceCreatedEventHandler;
import dev.jianmu.event.impl.WorkflowInstanceCreatedEvent;
import dev.jianmu.infrastructure.sse.WorkflowInstanceStatusSubscribeService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "jianmu.event", name = "type", havingValue = "local", matchIfMissing = true)
public class WorkflowInstanceCreatedEventHandlerImpl extends WorkflowInstanceCreatedEventHandler {
    public WorkflowInstanceCreatedEventHandlerImpl(WorkflowInstanceStatusSubscribeService workflowInstanceStatusSubscribeService) {
        super(workflowInstanceStatusSubscribeService);
    }

    @Async
    @EventListener
    @Override
    public void subscribe(WorkflowInstanceCreatedEvent event) {
        super.subscribe(event);
    }
}
