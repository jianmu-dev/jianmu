package dev.jianmu.api.eventhandler.local;

import dev.jianmu.api.eventhandler.AsyncInstanceStatusUpdatedEventHandler;
import dev.jianmu.event.impl.AsyncTaskInstanceStatusUpdatedEvent;
import dev.jianmu.infrastructure.sse.WorkflowInstanceStatusSubscribeService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "jianmu.event", name = "type", havingValue = "local", matchIfMissing = true)
public class AsyncTaskInstanceStatusUpdatedEventHandlerImpl extends AsyncInstanceStatusUpdatedEventHandler {
    public AsyncTaskInstanceStatusUpdatedEventHandlerImpl(WorkflowInstanceStatusSubscribeService workflowInstanceStatusSubscribeService) {
        super(workflowInstanceStatusSubscribeService);
    }

    @Async
    @EventListener
    @Override
    public void subscribe(AsyncTaskInstanceStatusUpdatedEvent event) {
        super.subscribe(event);
    }
}
