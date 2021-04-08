package dev.jianmu.infrastructure.messagequeue;

import dev.jianmu.task.aggregate.TaskInstance;
import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedTransferQueue;

/**
 * @class: TaskInstanceQueueInMemoryImpl
 * @description: 任务实例队列接口内存版本实现
 * @author: Ethan Liu
 * @create: 2021-04-02 22:34
 **/
@Component
public class TaskInstanceQueueInMemoryImpl implements TaskInstanceQueue {
    private final LinkedTransferQueue<TaskInstance> transferQueue = new LinkedTransferQueue<>();

    @Override
    public void put(TaskInstance taskInstance) {
        this.transferQueue.put(taskInstance);
    }

    @Override
    public TaskInstance take() throws InterruptedException {
        return this.transferQueue.take();
    }
}
