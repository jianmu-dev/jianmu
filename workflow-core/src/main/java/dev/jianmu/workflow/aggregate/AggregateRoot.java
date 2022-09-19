package dev.jianmu.workflow.aggregate;

import dev.jianmu.event.Event;
import dev.jianmu.workflow.event.DomainEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ethan Liu
 * @class AggregateRoot
 * @description 聚合根定义
 * @create 2021-03-25 07:43
 */
public class AggregateRoot {
    private final List<DomainEvent> events = new ArrayList<>();
    private final List<Event> sseEvents = new ArrayList<>();

    protected void raiseEvent(DomainEvent event) {
        this.events.add(event);
    }

    protected void raiseSseEvents(Event event) {
        this.sseEvents.add(event);
    }

    public void clear() {
        this.events.clear();
    }

    public void clearSseEvents() {
        this.sseEvents.clear();
    }

    public List<DomainEvent> getUncommittedDomainEvents() {
        return List.copyOf(this.events);
    }

    public List<Event> getUncommittedSseEvents() {
        return List.copyOf(this.sseEvents);
    }
}
