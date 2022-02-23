package dev.jianmu.infrastructure.mybatis.workflow;

import dev.jianmu.infrastructure.mapper.workflow.AsyncTaskInstanceMapper;
import dev.jianmu.workflow.aggregate.process.AsyncTaskInstance;
import dev.jianmu.workflow.repository.AsyncTaskInstanceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Ethan Liu
 * @class AsyncTaskInstanceRepositoryImpl
 * @description AsyncTaskInstanceRepositoryImpl
 * @create 2022-01-02 23:20
 */
@Repository
public class AsyncTaskInstanceRepositoryImpl implements AsyncTaskInstanceRepository {
    private final AsyncTaskInstanceMapper asyncTaskInstanceMapper;
    private final ApplicationEventPublisher publisher;

    public AsyncTaskInstanceRepositoryImpl(AsyncTaskInstanceMapper asyncTaskInstanceMapper, ApplicationEventPublisher publisher) {
        this.asyncTaskInstanceMapper = asyncTaskInstanceMapper;
        this.publisher = publisher;
    }

    @Override
    public Optional<AsyncTaskInstance> findById(String id) {
        return this.asyncTaskInstanceMapper.findById(id);
    }

    @Override
    public List<AsyncTaskInstance> findByInstanceId(String instanceId) {
        return this.asyncTaskInstanceMapper.findByInstanceId(instanceId);
    }

    @Override
    public List<AsyncTaskInstance> findByTriggerId(String triggerId) {
        return this.asyncTaskInstanceMapper.findByTriggerId(triggerId);
    }

    @Override
    public List<AsyncTaskInstance> findByTriggerIdAndTaskRef(String triggerId, String taskRef) {
        return this.asyncTaskInstanceMapper.findByTriggerIdAndTaskRef(triggerId, taskRef);
    }

    @Override
    public void add(AsyncTaskInstance asyncTaskInstance) {
        this.asyncTaskInstanceMapper.add(asyncTaskInstance);
        this.publisher.publishEvent(asyncTaskInstance);
    }

    @Override
    public void updateById(AsyncTaskInstance asyncTaskInstance) {
        this.asyncTaskInstanceMapper.updateById(asyncTaskInstance);
        this.publisher.publishEvent(asyncTaskInstance);
    }

    @Override
    public void updateAll(List<AsyncTaskInstance> asyncTaskInstances) {
        asyncTaskInstances.forEach(this.publisher::publishEvent);
    }

    @Override
    public void deleteByWorkflowInstanceId(String workflowInstanceId) {
        this.asyncTaskInstanceMapper.deleteByWorkflowInstanceId(workflowInstanceId);
    }

    @Override
    public void deleteByWorkflowRef(String workflowRef) {
        this.asyncTaskInstanceMapper.deleteByWorkflowRef(workflowRef);
    }
}
