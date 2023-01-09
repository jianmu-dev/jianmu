package dev.jianmu.api.eventhandler;

import dev.jianmu.event.Subscriber;
import dev.jianmu.event.impl.WatchDeferredResultTerminateEvent;
import dev.jianmu.infrastructure.worker.DeferredResultService;
import lombok.extern.slf4j.Slf4j;

/**
 * @class WatchDeferredResultTerminateEventHandler
 * @description WatchDeferredResultTerminateEventHandler
 * @author Daihw
 * @create 2023/1/9 3:10 下午
 */
@Slf4j
public class WatchDeferredResultTerminateEventHandler implements Subscriber<WatchDeferredResultTerminateEvent> {
    private final DeferredResultService deferredResultService;

    public WatchDeferredResultTerminateEventHandler(DeferredResultService deferredResultService) {
        this.deferredResultService = deferredResultService;
    }

    @Override
    public void subscribe(WatchDeferredResultTerminateEvent event) {
        log.info("Get WatchDeferredResultTerminateEvent here -------------------------");
        log.info(event.toString());
        this.deferredResultService.terminateDeferredResult(event.getWorkerId(), event.getBusinessId());
        log.info("-----------------------------------------------------");
    }
}
