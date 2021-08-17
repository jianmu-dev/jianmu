package dev.jianmu.api.eventhandler;

import dev.jianmu.application.service.EventBridgeApplication;
import dev.jianmu.application.service.ProjectApplication;
import dev.jianmu.eventbridge.aggregate.ConnectionEvent;
import dev.jianmu.eventbridge.aggregate.SourceEvent;
import dev.jianmu.eventbridge.aggregate.TargetEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @class: EventBridgeHandler
 * @description: EventBridgeHandler
 * @author: Ethan Liu
 * @create: 2021-08-14 20:22
 **/
@Component
@Slf4j
public class EventBridgeHandler {
    private final EventBridgeApplication eventBridgeApplication;
    private final ProjectApplication projectApplication;

    public EventBridgeHandler(EventBridgeApplication eventBridgeApplication, ProjectApplication projectApplication) {
        this.eventBridgeApplication = eventBridgeApplication;
        this.projectApplication = projectApplication;
    }

    @EventListener
    public void sourceEventHandle(SourceEvent sourceEvent) {
        sourceEvent.getPayload().getHeader().forEach((k, v) -> {
            log.info("header key: {}  value: {}", k, v);
        });
        this.eventBridgeApplication.dispatchEvent(sourceEvent);
    }

    @EventListener
    public void connectionEventHandle(ConnectionEvent connectionEvent) {
        this.eventBridgeApplication.eventHandling(connectionEvent);
    }

    @EventListener
    public void targetEventHandle(TargetEvent targetEvent) {
        log.info("get targetEvent here ID: {}", targetEvent.getId());
        log.info("get targetEvent here DestinationId: {}", targetEvent.getDestinationId());
        targetEvent.getEventParameters().forEach(eventParameter -> {
            log.info("eventParameter {} type is {}", eventParameter.getName(), eventParameter.getType());
        });
//        this.projectApplication.trigger(targetEvent.getDestinationId());
    }
}
