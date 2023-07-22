package dev.jianmu.infrastructure.lock.impl;

import dev.jianmu.infrastructure.lock.DistributedLock;
import jakarta.annotation.Resource;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;

/**
 * @author Daihw
 * @class RedissonLock
 * @description RedissonLock
 * @create 2022/12/9 11:13 上午
 */
@ConditionalOnProperty(prefix = "jianmu.event", name = "type", havingValue = "redis")
@Component
public class RedissonLock implements DistributedLock {
    @Resource
    private RedissonClient redissonClient;

    @Override
    public Lock getLock(Object object) {
        return redissonClient.getLock(object.toString());
    }
}
