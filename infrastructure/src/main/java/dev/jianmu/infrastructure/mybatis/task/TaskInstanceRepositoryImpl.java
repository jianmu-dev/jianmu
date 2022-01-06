package dev.jianmu.infrastructure.mybatis.task;

import dev.jianmu.infrastructure.mapper.task.TaskInstanceMapper;
import dev.jianmu.task.aggregate.TaskInstance;
import dev.jianmu.task.event.TaskInstanceCreatedEvent;
import dev.jianmu.task.event.TaskInstanceFailedEvent;
import dev.jianmu.task.event.TaskInstanceRunningEvent;
import dev.jianmu.task.event.TaskInstanceSucceedEvent;
import dev.jianmu.task.repository.TaskInstanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Ethan Liu
 * @class InstanceRepositoryImpl
 * @description 任务实例仓储实现类
 * @create 2021-03-25 20:54
 */
@Repository
public class TaskInstanceRepositoryImpl implements TaskInstanceRepository {
    private static final Logger logger = LoggerFactory.getLogger(TaskInstanceRepositoryImpl.class);
    private final TaskInstanceMapper taskInstanceMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    public TaskInstanceRepositoryImpl(
            TaskInstanceMapper taskInstanceMapper,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.taskInstanceMapper = taskInstanceMapper;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    private void publishEvent(TaskInstance taskInstance) {
        switch (taskInstance.getStatus()) {
            case WAITING:
                this.applicationEventPublisher.publishEvent(
                        TaskInstanceCreatedEvent.Builder.aTaskInstanceCreatedEvent()
                                .defKey(taskInstance.getDefKey())
                                .asyncTaskRef(taskInstance.getAsyncTaskRef())
                                .triggerId(taskInstance.getTriggerId())
                                .businessId(taskInstance.getBusinessId())
                                .taskInstanceId(taskInstance.getId())
                                .build()
                );
                break;
            case RUNNING:
                this.applicationEventPublisher.publishEvent(
                        TaskInstanceRunningEvent.Builder.aTaskInstanceRunningEvent()
                                .defKey(taskInstance.getDefKey())
                                .asyncTaskRef(taskInstance.getAsyncTaskRef())
                                .triggerId(taskInstance.getTriggerId())
                                .businessId(taskInstance.getBusinessId())
                                .taskInstanceId(taskInstance.getId())
                                .build()
                );
                break;
            case EXECUTION_FAILED:
                this.applicationEventPublisher.publishEvent(
                        TaskInstanceFailedEvent.Builder.aTaskInstanceFailedEvent()
                                .defKey(taskInstance.getDefKey())
                                .asyncTaskRef(taskInstance.getAsyncTaskRef())
                                .triggerId(taskInstance.getTriggerId())
                                .businessId(taskInstance.getBusinessId())
                                .taskInstanceId(taskInstance.getId())
                                .build()
                );
                break;
            case EXECUTION_SUCCEEDED:
                this.applicationEventPublisher.publishEvent(
                        TaskInstanceSucceedEvent.Builder.aTaskInstanceSucceedEvent()
                                .defKey(taskInstance.getDefKey())
                                .asyncTaskRef(taskInstance.getAsyncTaskRef())
                                .triggerId(taskInstance.getTriggerId())
                                .businessId(taskInstance.getBusinessId())
                                .taskInstanceId(taskInstance.getId())
                                .build()
                );
                break;
            case DISPATCH_FAILED:
                break;
            default:
                logger.warn("任务实例未知状态");
        }
    }

    @Override
    public void add(TaskInstance taskInstance) {
        this.taskInstanceMapper.add(taskInstance);
        this.publishEvent(taskInstance);
    }

    @Override
    public void updateStatus(TaskInstance taskInstance) {
        this.taskInstanceMapper.updateStatus(taskInstance);
        this.publishEvent(taskInstance);
    }

    @Override
    public void saveSucceeded(TaskInstance taskInstance) {
        this.taskInstanceMapper.saveSucceeded(taskInstance);
        this.publishEvent(taskInstance);
    }

    @Override
    public Optional<TaskInstance> findById(String instanceId) {
        return this.taskInstanceMapper.findById(instanceId);
    }

    @Override
    public Optional<TaskInstance> findByBusinessId(String businessId) {
        return this.taskInstanceMapper.findByBusinessId(businessId);
    }

    @Override
    public List<TaskInstance> findByTriggerId(String triggerId) {
        return this.taskInstanceMapper.findByTriggerId(triggerId);
    }

    @Override
    public List<TaskInstance> findRunningTask() {
        return this.taskInstanceMapper.findRunningTask();
    }

    @Override
    public List<TaskInstance> findByAsyncTaskRefAndBusinessId(String asyncTaskRef, String businessId) {
        return this.taskInstanceMapper.findByAsyncTaskRefAndBusinessId(asyncTaskRef, businessId);
    }

    @Override
    public List<TaskInstance> findAll(int pageNum, int pageSize) {
        return this.taskInstanceMapper.findAll(pageNum, pageSize);
    }

    @Override
    public void deleteByWorkflowRef(String workflowRef) {
        this.taskInstanceMapper.deleteByWorkflowRef(workflowRef);
    }

    @Override
    public void deleteByTriggerId(String triggerId) {
        this.taskInstanceMapper.deleteByTriggerId(triggerId);
    }
}
