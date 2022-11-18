package dev.jianmu.api.eventhandler.redis;

import dev.jianmu.api.eventhandler.WorkflowInstanceStatusUpdatedEventHandler;
import dev.jianmu.event.impl.WorkflowInstanceStatusUpdatedEvent;
import dev.jianmu.infrastructure.redis.RedisSubscriber;
import dev.jianmu.infrastructure.redis.annotation.RedisEventListener;
import dev.jianmu.infrastructure.sse.WorkflowInstanceStatusSubscribeService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "jianmu.event", name = "type", havingValue = "redis")
public class WorkflowInstanceStatusUpdatedEventHandlerImpl extends WorkflowInstanceStatusUpdatedEventHandler implements RedisSubscriber<WorkflowInstanceStatusUpdatedEvent> {
    public WorkflowInstanceStatusUpdatedEventHandlerImpl(WorkflowInstanceStatusSubscribeService workflowInstanceStatusSubscribeService) {
        super(workflowInstanceStatusSubscribeService);
    }

    @RedisEventListener
    @Override
    public void subscribe(WorkflowInstanceStatusUpdatedEvent event) {
        super.subscribe(event);
    }
}
