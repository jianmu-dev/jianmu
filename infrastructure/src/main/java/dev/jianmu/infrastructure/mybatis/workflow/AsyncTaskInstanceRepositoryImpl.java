package dev.jianmu.infrastructure.mybatis.workflow;

import dev.jianmu.infrastructure.GlobalProperties;
import dev.jianmu.infrastructure.exception.DBException;
import dev.jianmu.infrastructure.mapper.workflow.AsyncTaskInstanceBackupMapper;
import dev.jianmu.infrastructure.mapper.workflow.AsyncTaskInstanceMapper;
import dev.jianmu.workflow.aggregate.process.AsyncTaskInstance;
import dev.jianmu.workflow.repository.AsyncTaskInstanceRepository;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Repository
public class AsyncTaskInstanceRepositoryImpl implements AsyncTaskInstanceRepository {
    private final AsyncTaskInstanceMapper asyncTaskInstanceMapper;
    private final AsyncTaskInstanceBackupMapper asyncTaskInstanceBackupMapper;
    private final ApplicationEventPublisher publisher;
    private final boolean backup;

    public AsyncTaskInstanceRepositoryImpl(AsyncTaskInstanceMapper asyncTaskInstanceMapper,
                                           AsyncTaskInstanceBackupMapper asyncTaskInstanceBackupMapper,
                                           ApplicationEventPublisher publisher,
                                           GlobalProperties globalProperties
    ) {
        this.asyncTaskInstanceMapper = asyncTaskInstanceMapper;
        this.asyncTaskInstanceBackupMapper = asyncTaskInstanceBackupMapper;
        this.publisher = publisher;
        this.backup = globalProperties.getBackup();
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
    public Optional<AsyncTaskInstance> findByTriggerIdAndTaskRef(String triggerId, String taskRef) {
        return this.asyncTaskInstanceMapper.findByTriggerIdAndTaskRef(triggerId, taskRef);
    }

    @Override
    public void addAll(List<AsyncTaskInstance> asyncTaskInstances) {
        this.asyncTaskInstanceMapper.addAll(asyncTaskInstances);
        if (this.backup) {
            this.asyncTaskInstanceBackupMapper.addAll(asyncTaskInstances);
        }
    }

    @Override
    public void updateById(AsyncTaskInstance asyncTaskInstance) {
        this.asyncTaskInstanceMapper.updateById(asyncTaskInstance);
        if (this.backup) {
            this.asyncTaskInstanceBackupMapper.updateById(asyncTaskInstance);
        }
        this.publisher.publishEvent(asyncTaskInstance);
    }

    @Override
    public void succeedById(AsyncTaskInstance asyncTaskInstance, int version) {
        var succeed = this.asyncTaskInstanceMapper.succeedById(asyncTaskInstance, version);
        if (this.backup) {
            this.asyncTaskInstanceBackupMapper.succeedById(asyncTaskInstance, version);
        }
        if (!succeed) {
            throw new DBException.OptimisticLocking("未找到对应的乐观锁版本数据，无法完成数据更新");
        }
        this.publisher.publishEvent(asyncTaskInstance);
    }

    @Override
    public void activateById(AsyncTaskInstance asyncTaskInstance, int version) {
        var succeed = this.asyncTaskInstanceMapper.activateById(asyncTaskInstance, version);
        if (this.backup) {
            this.asyncTaskInstanceBackupMapper.activateById(asyncTaskInstance, version);
        }
        if (!succeed) {
            throw new DBException.OptimisticLocking("未找到对应的乐观锁版本数据，无法完成数据更新");
        }
        this.publisher.publishEvent(asyncTaskInstance);
    }

    @Override
    public void retryById(AsyncTaskInstance asyncTaskInstance, int version) {
        var succeed = this.asyncTaskInstanceMapper.activateById(asyncTaskInstance, version);
        if (this.backup) {
            this.asyncTaskInstanceBackupMapper.activateById(asyncTaskInstance, version);
        }
        if (!succeed) {
            throw new DBException.OptimisticLocking("未找到对应的乐观锁版本数据，无法完成数据更新");
        }
        this.publisher.publishEvent(asyncTaskInstance);
    }

    @Override
    public void ignoreById(AsyncTaskInstance asyncTaskInstance) {
        int version = this.asyncTaskInstanceMapper.getVersion(asyncTaskInstance.getId());
        var succeed = this.asyncTaskInstanceMapper.activateById(asyncTaskInstance, version);
        if (this.backup) {
            this.asyncTaskInstanceBackupMapper.activateById(asyncTaskInstance, version);
        }
        if (!succeed) {
            throw new DBException.OptimisticLocking("未找到对应的乐观锁版本数据，无法完成数据更新");
        }
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
