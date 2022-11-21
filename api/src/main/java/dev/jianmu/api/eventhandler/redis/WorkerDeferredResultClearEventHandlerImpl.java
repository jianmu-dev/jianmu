package dev.jianmu.api.eventhandler.redis;

import dev.jianmu.api.eventhandler.WorkerDeferredResultClearEventHandler;
import dev.jianmu.event.impl.WorkerDeferredResultClearEvent;
import dev.jianmu.infrastructure.redis.RedisSubscriber;
import dev.jianmu.infrastructure.redis.annotation.RedisEventListener;
import dev.jianmu.infrastructure.worker.DeferredResultService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * @author Daihw
 * @class WorkerDeferredResultClearEventHandlerImpl
 * @description WorkerDeferredResultClearEventHandlerImpl
 * @create 2022/11/17 3:32 下午
 */
@Component
@ConditionalOnProperty(prefix = "jianmu.event", name = "type", havingValue = "redis")
public class WorkerDeferredResultClearEventHandlerImpl extends WorkerDeferredResultClearEventHandler implements RedisSubscriber<WorkerDeferredResultClearEvent> {
    public WorkerDeferredResultClearEventHandlerImpl(DeferredResultService deferredResultService) {
        super(deferredResultService);
    }

    @RedisEventListener
    @Override
    public void subscribe(WorkerDeferredResultClearEvent event) {
        super.subscribe(event);
    }
}
