package dev.jianmu.api.eventhandler.redis;

import dev.jianmu.api.eventhandler.WorkflowInstanceCreatedEventHandler;
import dev.jianmu.event.impl.WorkflowInstanceCreatedEvent;
import dev.jianmu.infrastructure.redis.RedisSubscriber;
import dev.jianmu.infrastructure.redis.annotation.RedisEventListener;
import dev.jianmu.infrastructure.sse.WorkflowInstanceStatusSubscribeService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "jianmu.event", name = "type", havingValue = "redis")
public class WorkflowInstanceCreatedEventHandlerImpl extends WorkflowInstanceCreatedEventHandler implements RedisSubscriber<WorkflowInstanceCreatedEvent> {
    public WorkflowInstanceCreatedEventHandlerImpl(WorkflowInstanceStatusSubscribeService workflowInstanceStatusSubscribeService) {
        super(workflowInstanceStatusSubscribeService);
    }

    @RedisEventListener
    @Override
    public void subscribe(WorkflowInstanceCreatedEvent event) {
        super.subscribe(event);
    }
}
