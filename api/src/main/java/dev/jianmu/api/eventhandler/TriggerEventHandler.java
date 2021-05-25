package dev.jianmu.api.eventhandler;

import dev.jianmu.trigger.event.TriggerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @class: TriggerEventHandler
 * @description: TriggerEventHandler
 * @author: Ethan Liu
 * @create: 2021-05-25 08:44
 **/
@Component
public class TriggerEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(TriggerEventHandler.class);

    @EventListener
    public void handleTriggerEvent(TriggerEvent triggerEvent) {
        logger.info("Got TriggerEvent: {}", triggerEvent.getTriggerId());
    }
}
