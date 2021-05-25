package dev.jianmu.api.eventhandler;

import dev.jianmu.application.service.ProjectApplication;
import dev.jianmu.trigger.event.TriggerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @class: TriggerEventHandler
 * @description: TriggerEventHandler
 * @author: Ethan Liu
 * @create: 2021-05-25 08:44
 **/
@Component
public class TriggerEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(TriggerEventHandler.class);

    private final ProjectApplication projectApplication;

    public TriggerEventHandler(ProjectApplication projectApplication) {
        this.projectApplication = projectApplication;
    }

    @EventListener
    public void handleTriggerEvent(TriggerEvent triggerEvent) {
        logger.info("Got TriggerEvent: {} at: {}", triggerEvent, LocalDateTime.now());
        this.projectApplication.triggerFromCron(triggerEvent.getTriggerId());
    }
}
