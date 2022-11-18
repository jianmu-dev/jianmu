package dev.jianmu.infrastructure.redis.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import static dev.jianmu.infrastructure.redis.processor.RedisEventListenerAnnotationBeanPostProcessor.ADAPTERS;

/**
 * @author Daihw
 * @class RedisConfig
 * @description RedisConfig
 * @create 2022/11/17 3:36 下午
 */
@ConditionalOnProperty(prefix = "jianmu.event", name = "type", havingValue = "redis")
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        var redisTemplate = new RedisTemplate<String, Object>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return redisTemplate;
    }

    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
        // 订阅
        // TODO ADAPTERS待优化
        ADAPTERS.forEach((key, value) ->
                redisMessageListenerContainer.addMessageListener(value, new PatternTopic(key)));
        // 序列化
        var serializer = new Jackson2JsonRedisSerializer<>(String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        serializer.setObjectMapper(objectMapper);
        redisMessageListenerContainer.setTopicSerializer(serializer);
        return redisMessageListenerContainer;
    }
}
