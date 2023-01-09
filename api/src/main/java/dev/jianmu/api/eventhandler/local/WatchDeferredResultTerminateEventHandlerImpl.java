package dev.jianmu.api.eventhandler.local;

import dev.jianmu.api.eventhandler.WatchDeferredResultTerminateEventHandler;
import dev.jianmu.event.impl.WatchDeferredResultTerminateEvent;
import dev.jianmu.infrastructure.worker.DeferredResultService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author Daihw
 * @class WatchDeferredResultTerminateEventHandlerImpl
 * @description WatchDeferredResultTerminateEventHandlerImpl
 * @create 2023/1/9 3:14 下午
 */
@Component
@ConditionalOnProperty(prefix = "jianmu.event", name = "type", havingValue = "local", matchIfMissing = true)
public class WatchDeferredResultTerminateEventHandlerImpl extends WatchDeferredResultTerminateEventHandler {
    public WatchDeferredResultTerminateEventHandlerImpl(DeferredResultService deferredResultService) {
        super(deferredResultService);
    }

    @Async
    @EventListener
    @Override
    public void subscribe(WatchDeferredResultTerminateEvent event) {
        super.subscribe(event);
    }
}
