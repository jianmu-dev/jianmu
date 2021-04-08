package dev.jianmu.task.repository;

import dev.jianmu.task.aggregate.TaskInstance;
import dev.jianmu.task.aggregate.InstanceStatus;

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

    List<TaskInstance> findByKeyVersionAndBusinessId(String keyVersion, String businessId);

    List<TaskInstance> findByKeyVersion(String keyVersion);

    List<TaskInstance> findByStatus(InstanceStatus status);

    List<TaskInstance> findAll(int pageNum, int pageSize);
}
