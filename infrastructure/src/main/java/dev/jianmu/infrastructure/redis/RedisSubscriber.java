package dev.jianmu.infrastructure.redis;

import dev.jianmu.event.Event;
import dev.jianmu.event.Subscriber;
import dev.jianmu.infrastructure.jackson2.JsonUtil;

import java.lang.reflect.Method;

/**
 * @author Daihw
 * @class RedisMessage
 * @description RedisMessage
 * @create 2022/11/17 4:40 下午
 */
public interface RedisSubscriber<E extends Event> extends Subscriber<E> {
    default void receiveMessage(String message) {
        Class<?> cla = null;
        for (Method method : this.getClass().getMethods()) {
            if (method.getName().equals("subscribe")) {
                cla = method.getParameterTypes()[0];
                break;
            }
        }
        assert cla != null;
        var event = JsonUtil.stringToJson(message, cla);
        this.subscribe((E) event);
    }
}
