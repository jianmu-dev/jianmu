package dev.jianmu.infrastructure.mybatis.task;

import dev.jianmu.infrastructure.mapper.task.TaskInstanceMapper;
import dev.jianmu.infrastructure.mapper.task.TaskInstanceParameterMapper;
import dev.jianmu.task.aggregate.InstanceStatus;
import dev.jianmu.task.aggregate.TaskInstance;
import dev.jianmu.task.event.TaskInstanceFailedEvent;
import dev.jianmu.task.event.TaskInstanceSucceedEvent;
import dev.jianmu.task.repository.TaskInstanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

/**
 * @class: InstanceRepositoryImpl
 * @description: 任务实例仓储实现类
 * @author: Ethan Liu
 * @create: 2021-03-25 20:54
 **/
@Repository
public class TaskInstanceRepositoryImpl implements TaskInstanceRepository {
    private static final Logger logger = LoggerFactory.getLogger(TaskInstanceRepositoryImpl.class);
    private final TaskInstanceMapper taskInstanceMapper;
    private final TaskInstanceParameterMapper taskInstanceParameterMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Inject
    public TaskInstanceRepositoryImpl(
            TaskInstanceMapper taskInstanceMapper,
            TaskInstanceParameterMapper taskInstanceParameterMapper,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.taskInstanceMapper = taskInstanceMapper;
        this.taskInstanceParameterMapper = taskInstanceParameterMapper;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void add(TaskInstance taskInstance) {
        this.taskInstanceMapper.add(taskInstance);
        if (taskInstance.getParameters().size() > 0) {
            this.taskInstanceParameterMapper.addAll(taskInstance.getId(), taskInstance.getParameters());
        }
        this.applicationEventPublisher.publishEvent(taskInstance);
    }

    @Override
    public void updateStatus(TaskInstance taskInstance) {
        this.taskInstanceMapper.updateStatus(taskInstance);
        if (taskInstance.getStatus().equals(InstanceStatus.EXECUTION_FAILED)) {
            this.applicationEventPublisher.publishEvent(
                    TaskInstanceFailedEvent.Builder.aTaskInstanceFailedEvent()
                            .defKey(taskInstance.getDefKey())
                            .asyncTaskRef(taskInstance.getAsyncTaskRef())
                            .businessId(taskInstance.getBusinessId())
                            .taskInstanceId(taskInstance.getId())
                            .build()
            );
        } else if (taskInstance.getStatus().equals(InstanceStatus.EXECUTION_SUCCEEDED)) {
            this.applicationEventPublisher.publishEvent(
                    TaskInstanceSucceedEvent.Builder.aTaskInstanceSucceedEvent()
                            .defKey(taskInstance.getDefKey())
                            .asyncTaskRef(taskInstance.getAsyncTaskRef())
                            .businessId(taskInstance.getBusinessId())
                            .taskInstanceId(taskInstance.getId())
                            .build()
            );
        }
    }

    @Override
    public Optional<TaskInstance> findById(String instanceId) {
        return this.taskInstanceMapper.findById(instanceId);
    }

    @Override
    public List<TaskInstance> findByBusinessId(String businessId) {
        return this.taskInstanceMapper.findByBusinessId(businessId);
    }

    @Override
    public List<TaskInstance> findByAsyncTaskRefAndBusinessId(String asyncTaskRef, String businessId) {
        return this.taskInstanceMapper.findByAsyncTaskRefAndBusinessId(asyncTaskRef, businessId);
    }

    @Override
    public List<TaskInstance> findByDefKey(String defKey) {
        return this.taskInstanceMapper.findByDefKey(defKey);
    }

    @Override
    public List<TaskInstance> findByStatus(InstanceStatus status) {
        return this.taskInstanceMapper.findByStatus(status);
    }

    @Override
    public List<TaskInstance> findAll(int pageNum, int pageSize) {
        return this.taskInstanceMapper.findAll(pageNum, pageSize);
    }
}
