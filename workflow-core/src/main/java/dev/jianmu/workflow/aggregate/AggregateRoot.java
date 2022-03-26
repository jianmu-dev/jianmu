package dev.jianmu.workflow.aggregate;

import dev.jianmu.workflow.event.DomainEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @class AggregateRoot
 * @description 聚合根定义
 * @author Ethan Liu
 * @create 2021-03-25 07:43
*/
public class AggregateRoot {
    private final List<DomainEvent> events = new ArrayList<>();

    protected void raiseEvent(DomainEvent event) {
        this.events.add(event);
    }

    public void clear() {
        this.events.clear();
    }

    public List<DomainEvent> getUncommittedDomainEvents() {
        return List.copyOf(this.events);
    }
}
