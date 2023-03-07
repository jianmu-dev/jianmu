package dev.jianmu.api.eventhandler;

import dev.jianmu.application.service.CacheApplication;
import dev.jianmu.task.event.VolumeCreatedEvent;
import dev.jianmu.task.event.VolumeDeletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author Daihw
 * @class VolumeEventHandler
 * @description VolumeEventHandler
 * @create 2023/3/1 6:08 下午
 */
@Component
@Slf4j
public class VolumeEventHandler {
    private final CacheApplication cacheApplication;

    public VolumeEventHandler(CacheApplication cacheApplication) {
        this.cacheApplication = cacheApplication;
    }

    @EventListener
    public void handlerVolumeCreatedEvent(VolumeCreatedEvent event) {
        this.cacheApplication.create(event.getWorkflowRef(), event.getName());
    }

    @EventListener
    public void handlerVolumeDeletedEvent(VolumeDeletedEvent event) {
        switch (event.deletedType) {
            case ID -> this.cacheApplication.deleteById(event.getId());
            case NAME -> this.cacheApplication.deleteByNameAndWorkflowRef(event.getName(), event.getWorkflowRef());
            case REF -> this.cacheApplication.deleteByWorkflowRef(event.getWorkflowRef());
        }
    }
}
