package dev.jianmu.api.eventhandler.local;

import dev.jianmu.api.eventhandler.WorkerDeferredResultClearEventHandler;
import dev.jianmu.event.impl.WorkerDeferredResultClearEvent;
import dev.jianmu.infrastructure.worker.DeferredResultService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author Daihw
 * @class WorkerDeferredResultClearEventHandlerImpl
 * @description WorkerDeferredResultClearEventHandlerImpl
 * @create 2022/11/17 3:32 下午
 */
@Component
@ConditionalOnProperty(prefix = "jianmu.event", name = "type", havingValue = "local", matchIfMissing = true)
public class WorkerDeferredResultClearEventHandlerImpl extends WorkerDeferredResultClearEventHandler {
    public WorkerDeferredResultClearEventHandlerImpl(DeferredResultService deferredResultService) {
        super(deferredResultService);
    }

    @Async
    @EventListener
    @Override
    public void subscribe(WorkerDeferredResultClearEvent event) {
        super.subscribe(event);
    }
}
