package dev.jianmu.event;

public interface Subscriber<E extends Event> {
    void subscribe(E event);
}
