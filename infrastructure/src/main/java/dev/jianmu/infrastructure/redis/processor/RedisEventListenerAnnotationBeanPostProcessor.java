package dev.jianmu.infrastructure.redis.processor;

import dev.jianmu.event.Event;
import dev.jianmu.infrastructure.redis.annotation.RedisEventListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Daihw
 * @class RedisEventListenerAnnotationBeanPostProcessor
 * @description RedisEventListenerAnnotationBeanPostProcessor
 * @create 2022/11/17 3:40 下午
 */
@ConditionalOnProperty(prefix = "jianmu.event", name = "type", havingValue = "redis")
@Component
public class RedisEventListenerAnnotationBeanPostProcessor implements BeanPostProcessor {
    public static final Map<String, MessageListenerAdapter> ADAPTERS = new HashMap<>();

    @Nullable
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        var methods = bean.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (!method.isAnnotationPresent(RedisEventListener.class)) {
                continue;
            }
            if (method.isBridge()) {
                continue;
            }
            Event event;
            try {
                event = (Event) method.getParameterTypes()[0].getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            var topic = event.getTopic();
            var messageListenerAdapter = new MessageListenerAdapter(bean, "receiveMessage");
            if (ADAPTERS.containsKey(topic)) {
                throw new RuntimeException("redis topic is repeated, " + topic);
            }
            ADAPTERS.put(topic, messageListenerAdapter);
            // fix: Cannot invoke "org.springframework.data.redis.listener.adapter.MessageListenerAdapter$MethodInvoker.getMethodName()" because "this.invoker" is null
            messageListenerAdapter.afterPropertiesSet();
        }
        return bean;
    }

    @Nullable
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}

