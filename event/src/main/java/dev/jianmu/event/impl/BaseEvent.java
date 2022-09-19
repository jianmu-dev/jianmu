package dev.jianmu.event.impl;

import dev.jianmu.event.Event;

public abstract class BaseEvent implements Event {
    private final String eventName = this.getClass().getSimpleName();
    private final Long timestamp = System.currentTimeMillis();

    public String getEventName() {
        return eventName;
    }

    public Long getTimestamp() {
        return timestamp;
    }
}
