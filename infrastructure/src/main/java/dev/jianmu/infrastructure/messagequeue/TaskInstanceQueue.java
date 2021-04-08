package dev.jianmu.infrastructure.messagequeue;

import dev.jianmu.task.aggregate.TaskInstance;

/**
 * @class: TaskInstanceQueue
 * @description: 任务实例队列接口
 * @author: Ethan Liu
 * @create: 2021-04-02 22:30
 **/
public interface TaskInstanceQueue {
    void put(TaskInstance taskInstance);

    TaskInstance take() throws InterruptedException;
}
