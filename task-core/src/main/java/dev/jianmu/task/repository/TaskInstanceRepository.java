package dev.jianmu.task.repository;

import dev.jianmu.task.aggregate.InstanceStatus;
import dev.jianmu.task.aggregate.TaskInstance;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author Ethan Liu
 * @class InstanceRepository
 * @description 任务实例仓储接口
 * @create 2021-03-25 19:27
 */
public interface TaskInstanceRepository {
    void add(TaskInstance taskInstance);

    void updateStatus(TaskInstance taskInstance);

    void updateWorkerId(TaskInstance taskInstance);

    boolean acceptTask(TaskInstance taskInstance);

    void terminate(TaskInstance taskInstance);

    void saveSucceeded(TaskInstance taskInstance);

    void commitEvent(TaskInstance taskInstance);

    Optional<TaskInstance> findById(String instanceId);

    Optional<TaskInstance> findByBusinessIdAndMaxSerialNo(String businessId);

    List<TaskInstance> findByTriggerId(String triggerId);

    List<TaskInstance> findRunningTask();

    List<TaskInstance> findByBusinessId(String businessId);

    List<TaskInstance> findAll(int pageNum, int pageSize);

    void deleteByWorkflowRef(String workflowRef);

    void deleteByTriggerId(String triggerId);

    Optional<TaskInstance> findByWorkerIdAndTriggerIdLimit(String workerId, String triggerId);

    Optional<TaskInstance> findByBusinessIdAndVersion(String businessId, int version);

    List<TaskInstance> findByTriggerIdAndStatus(String triggerId, InstanceStatus status);

    List<TaskInstance> findIdAndRefByWorkflowRef(String workflowRef);
}
