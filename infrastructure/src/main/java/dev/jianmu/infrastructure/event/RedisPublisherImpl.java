package dev.jianmu.infrastructure.event;

import dev.jianmu.event.Event;
import dev.jianmu.event.Publisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Daihw
 * @class RedisPublisherImpl
 * @description RedisPublisherImpl
 * @create 2022/11/17 3:26 下午
 */
@Component
@ConditionalOnProperty(prefix = "jianmu.event", name = "type", havingValue = "redis")
public class RedisPublisherImpl implements Publisher {
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisPublisherImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void publish(Event event) {
        this.redisTemplate.convertAndSend(event.getTopic(), event);
    }
}
