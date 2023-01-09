package dev.jianmu.api.eventhandler.redis;

import dev.jianmu.api.eventhandler.WatchDeferredResultTerminateEventHandler;
import dev.jianmu.event.impl.WatchDeferredResultTerminateEvent;
import dev.jianmu.infrastructure.redis.RedisSubscriber;
import dev.jianmu.infrastructure.redis.annotation.RedisEventListener;
import dev.jianmu.infrastructure.worker.DeferredResultService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * @author Daihw
 * @class WatchDeferredResultTerminateEventHandlerImpl
 * @description WatchDeferredResultTerminateEventHandlerImpl
 * @create 2023/1/9 3:14 下午
 */
@Component
@ConditionalOnProperty(prefix = "jianmu.event", name = "type", havingValue = "redis")
public class WatchDeferredResultTerminateEventHandlerImpl extends WatchDeferredResultTerminateEventHandler implements RedisSubscriber<WatchDeferredResultTerminateEvent> {
    public WatchDeferredResultTerminateEventHandlerImpl(DeferredResultService deferredResultService) {
        super(deferredResultService);
    }

    @RedisEventListener
    @Override
    public void subscribe(WatchDeferredResultTerminateEvent event) {
        super.subscribe(event);
    }
}
