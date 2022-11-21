package dev.jianmu.infrastructure.redis.annotation;

import java.lang.annotation.*;

/**
 * @author Daihw
 * @class RedisEventListener
 * @description RedisEventListener
 * @create 2022/11/17 1:45 下午
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisEventListener {
}

