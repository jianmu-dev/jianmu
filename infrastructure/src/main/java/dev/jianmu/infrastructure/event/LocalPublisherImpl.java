package dev.jianmu.infrastructure.event;

import dev.jianmu.event.Event;
import dev.jianmu.event.Publisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * @class LocalPublisherImpl
 * @description LocalPublisherImpl
 * @author Daihw
 * @create 2022/11/17 3:51 下午
 */
@Component
@ConditionalOnProperty(prefix = "jianmu.event", name = "type", havingValue = "local", matchIfMissing = true)
public class LocalPublisherImpl implements Publisher {
    private final ApplicationEventPublisher publisher;

    public LocalPublisherImpl(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publish(Event event) {
        this.publisher.publishEvent(event);
    }
}
