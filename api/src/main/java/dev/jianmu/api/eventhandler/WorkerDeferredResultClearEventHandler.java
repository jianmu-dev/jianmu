package dev.jianmu.api.eventhandler;

import dev.jianmu.event.Subscriber;
import dev.jianmu.event.impl.WorkerDeferredResultClearEvent;
import dev.jianmu.infrastructure.worker.DeferredResultService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Daihw
 * @class WorkerDeferredResultClearEventHandler
 * @description WorkerDeferredResultClearEventHandler
 * @create 2022/11/17 3:29 下午
 */
@Slf4j
public class WorkerDeferredResultClearEventHandler implements Subscriber<WorkerDeferredResultClearEvent> {
    private final DeferredResultService deferredResultService;

    public WorkerDeferredResultClearEventHandler(DeferredResultService deferredResultService) {
        this.deferredResultService = deferredResultService;
    }

    @Override
    public void subscribe(WorkerDeferredResultClearEvent event) {
        log.info("Get WorkerDeferredResultClearEvent here -------------------------");
        log.info(event.toString());
        this.deferredResultService.clearWorker(event.getWorkerId());
        log.info("-----------------------------------------------------");
    }
}
