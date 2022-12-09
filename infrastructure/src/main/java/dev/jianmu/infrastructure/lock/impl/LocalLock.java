package dev.jianmu.infrastructure.lock.impl;

import dev.jianmu.infrastructure.lock.DistributedLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @class LocalLock
 * @description LocalLock
 * @author Daihw
 * @create 2022/12/9 11:08 上午
 */
@ConditionalOnProperty(prefix = "jianmu.event", name = "type", havingValue = "local", matchIfMissing = true)
@Component
public class LocalLock implements DistributedLock {
    private static final Map<Object, Lock> PROJECT_LOCK_MAP = new ConcurrentHashMap<>();

    @Override
    public Lock getLock(Object object) {
        PROJECT_LOCK_MAP.putIfAbsent(object, new ReentrantLock());
        return PROJECT_LOCK_MAP.get(object);
    }
}
