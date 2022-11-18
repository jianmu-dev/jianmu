package dev.jianmu.event.impl;

import dev.jianmu.event.Event;
import lombok.Getter;

@Getter
public abstract class BaseEvent implements Event {
    private final String eventName = this.getClass().getSimpleName();
    private final long timestamp = System.currentTimeMillis();

    @Override
    public String getTopic() {
        return this.eventName;
    }
}
