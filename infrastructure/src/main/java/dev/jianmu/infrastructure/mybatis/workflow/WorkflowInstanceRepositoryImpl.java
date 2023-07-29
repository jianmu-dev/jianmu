package dev.jianmu.infrastructure.mybatis.workflow;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import dev.jianmu.infrastructure.mapper.workflow.WorkflowInstanceMapper;
import dev.jianmu.workflow.aggregate.process.ProcessStatus;
import dev.jianmu.workflow.aggregate.process.WorkflowInstance;
import dev.jianmu.workflow.repository.WorkflowInstanceRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Ethan Liu
 * @class WorkflowInstanceRepositoryImpl
 * @description 流程实例仓储实现类
 * @create 2021-03-18 08:38
 */
@Slf4j
@Repository
public class WorkflowInstanceRepositoryImpl implements WorkflowInstanceRepository {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowInstanceRepositoryImpl.class);
    @Resource
    private ApplicationEventPublisher publisher;

    @Resource
    private WorkflowInstanceMapper workflowInstanceMapper;

    @Override
    public List<WorkflowInstance> findByRefAndStatuses(String workflowRef, List<ProcessStatus> statuses) {
        return this.workflowInstanceMapper.findByRefAndVersionAndStatuses(workflowRef, statuses);
    }

    @Override
    public Optional<WorkflowInstance> findByRefAndStatusAndSerialNoMin(String workflowRef, ProcessStatus status) {
        return this.workflowInstanceMapper.findByRefAndStatusAndSerialNoMin(workflowRef, status);
    }

    @Override
    public Optional<WorkflowInstance> findById(String instanceId) {
        return this.workflowInstanceMapper.findById(instanceId);
    }

    @Override
    public Optional<WorkflowInstance> findByTriggerId(String triggerId) {
        return this.workflowInstanceMapper.findByTriggerId(triggerId);
    }

    @Override
    public void add(WorkflowInstance workflowInstance) {
        this.workflowInstanceMapper.add(workflowInstance, 1);
        publisher.publishEvent(workflowInstance);
    }

    @Override
    public void save(WorkflowInstance workflowInstance) {
        this.workflowInstanceMapper.save(workflowInstance);
        this.publisher.publishEvent(workflowInstance);
    }

    @Override
    public boolean running(WorkflowInstance workflowInstance) {
        // 基于数据库行级锁，防止任务重复创建
        // fix: https://gitee.com/jianmu-dev/jianmu/issues/I6691G
        if (this.workflowInstanceMapper.running(workflowInstance)) {
            this.publisher.publishEvent(workflowInstance);
            return true;
        }
        log.warn("防止任务重复创建");
        return false;
    }

    @Override
    public void commitEvents(WorkflowInstance workflowInstance) {
        this.publisher.publishEvent(workflowInstance);
    }

    @Override
    public List<WorkflowInstance> findAll(int pageNum, int pageSize) {
        return this.workflowInstanceMapper.findAll(pageNum, pageSize);
    }

    @Override
    public List<WorkflowInstance> findByRef(String workflowRef) {
        return this.workflowInstanceMapper.findByRef(workflowRef);
    }

    @Override
    public Optional<WorkflowInstance> findByRefAndSerialNoMax(String workflowRef) {
        return this.workflowInstanceMapper.findByRefAndSerialNoMax(workflowRef);
    }

    @Override
    public void deleteByWorkflowRef(String workflowRef) {
        this.workflowInstanceMapper.deleteByWorkflowRef(workflowRef);
    }

    @Override
    public void deleteById(String id) {
        this.workflowInstanceMapper.deleteById(id);
    }

    @Override
    public List<WorkflowInstance> findByWorkflowAndRunningStatusOffset(String workflowRef, Long offset) {
        return this.workflowInstanceMapper.findByWorkflowAndRunningStatusOffset(workflowRef, offset);
    }

    @Override
    public List<WorkflowInstance> findByRefOffset(String workflowRef, long offset) {
        return this.workflowInstanceMapper.findByRefOffset(workflowRef, offset);
    }

    @Override
    public List<WorkflowInstance> findByWorkflowRefLimit(String workflowRef, long offset) {
        return this.workflowInstanceMapper.findByWorkflowRef(workflowRef, offset);
    }

    public PageInfo<WorkflowInstance> findPageByWorkflowRef(int pageNum, int pageSize, String workflowRef) {
        return PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(() -> this.workflowInstanceMapper.findPageByWorkflowRef(workflowRef));
    }
}
