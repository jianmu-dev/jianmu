package dev.jianmu.workflow.event;

/**
 * @program: workflow
 * @description: 领域事件订阅者
 * @author: Ethan Liu
 * @create: 2021-01-21 20:40
 **/
public interface DomainEventSubscriber<T extends DomainEvent> {
    void handle(T event);
}
