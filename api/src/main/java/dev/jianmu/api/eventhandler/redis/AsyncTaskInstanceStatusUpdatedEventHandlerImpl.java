package dev.jianmu.api.eventhandler.redis;

import dev.jianmu.api.eventhandler.AsyncInstanceStatusUpdatedEventHandler;
import dev.jianmu.event.impl.AsyncTaskInstanceStatusUpdatedEvent;
import dev.jianmu.infrastructure.redis.RedisSubscriber;
import dev.jianmu.infrastructure.redis.annotation.RedisEventListener;
import dev.jianmu.infrastructure.sse.WorkflowInstanceStatusSubscribeService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "jianmu.event", name = "type", havingValue = "redis")
public class AsyncTaskInstanceStatusUpdatedEventHandlerImpl extends AsyncInstanceStatusUpdatedEventHandler implements RedisSubscriber<AsyncTaskInstanceStatusUpdatedEvent> {
    public AsyncTaskInstanceStatusUpdatedEventHandlerImpl(WorkflowInstanceStatusSubscribeService workflowInstanceStatusSubscribeService) {
        super(workflowInstanceStatusSubscribeService);
    }

    @RedisEventListener
    @Override
    public void subscribe(AsyncTaskInstanceStatusUpdatedEvent event) {
        super.subscribe(event);
    }
}
