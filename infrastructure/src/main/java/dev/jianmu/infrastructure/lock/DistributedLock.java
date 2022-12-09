package dev.jianmu.infrastructure.lock;

import java.util.concurrent.locks.Lock;

/**
 * @class DistributedLock
 * @description DistributedLock
 * @author Daihw
 * @create 2022/12/9 11:06 上午
 */
public interface DistributedLock {
    Lock getLock(Object object);
}
