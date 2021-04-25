package dev.jianmu.task.repository;

import dev.jianmu.task.aggregate.TaskInstance;

import java.util.List;
import java.util.Optional;

/**
 * @class: InstanceRepository
 * @description: 任务实例仓储接口
 * @author: Ethan Liu
 * @create: 2021-03-25 19:27
 **/
public interface TaskInstanceRepository {
    void add(TaskInstance taskInstance);

    void updateStatus(TaskInstance taskInstance);

    Optional<TaskInstance> findById(String instanceId);

    List<TaskInstance> findByBusinessId(String businessId);

    List<TaskInstance> findByAsyncTaskRefAndBusinessId(String asyncTaskRef, String businessId);

    Optional<TaskInstance> limitByAsyncTaskRefAndBusinessId(String asyncTaskRef, String businessId);

    List<TaskInstance> findAll(int pageNum, int pageSize);
}
