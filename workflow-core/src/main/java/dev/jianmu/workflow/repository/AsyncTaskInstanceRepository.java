package dev.jianmu.workflow.repository;

import dev.jianmu.workflow.aggregate.process.AsyncTaskInstance;

import java.util.List;
import java.util.Optional;

/**
 * @author Ethan Liu
 * @class AsyncTaskInstanceRepository
 * @description AsyncTaskInstanceRepository
 * @create 2021-12-30 10:51
 */
public interface AsyncTaskInstanceRepository {
    Optional<AsyncTaskInstance> findById(String id);

    List<AsyncTaskInstance> findByInstanceId(String instanceId);

    List<AsyncTaskInstance> findByTriggerId(String triggerId);

    void add(AsyncTaskInstance asyncTaskInstance);

    void updateById(AsyncTaskInstance asyncTaskInstance);

    void updateAll(List<AsyncTaskInstance> asyncTaskInstances);
}
