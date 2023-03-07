package dev.jianmu.infrastructure.mybatis.workflow;

import dev.jianmu.infrastructure.exception.DBException;
import dev.jianmu.infrastructure.mapper.workflow.WorkflowMapper;
import dev.jianmu.workflow.aggregate.definition.Workflow;
import dev.jianmu.workflow.repository.WorkflowRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * @class WorkflowRepositoryImpl
 * @description 流程定义仓储实现类
 * @author Ethan Liu
 * @create 2021-02-12 21:35
*/
@Repository
public class WorkflowRepositoryImpl implements WorkflowRepository {

    @Resource
    private WorkflowMapper workflowMapper;
    @Resource
    private ApplicationEventPublisher publisher;

    @Override
    public Optional<Workflow> findByRefAndVersion(String ref, String version) {
        return this.workflowMapper.findByRefAndVersion(ref + version);
    }

    @Override
    public Optional<Workflow> findByRefVersion(String refVersion) {
        return this.workflowMapper.findByRefAndVersion(refVersion);
    }

    @Override
    public List<Workflow> findByRefVersions(List<String> refVersions) {
        return this.workflowMapper.findByRefAndVersions(refVersions);
    }

    @Override
    public List<Workflow> findByRef(String ref) {
        return this.workflowMapper.findByRef(ref);
    }

    @Override
    public Workflow add(Workflow workflow) {
        this.workflowMapper.add(workflow);
        Optional<Workflow> workflowOptional = this.workflowMapper
                .findByRefAndVersion(workflow.getRef() + workflow.getVersion());
        return workflowOptional.orElseThrow(() -> new DBException.InsertFailed("流程定义插入失败"));
    }

    @Override
    public void deleteByRefAndVersion(String ref, String version) {
        this.workflowMapper.deleteByRefAndVersion(ref + version);
    }

    @Override
    public void deleteByRef(String ref) {
        this.workflowMapper.deleteByRef(ref);
    }

    @Override
    public void commitEvents(Workflow workflow) {
        this.publisher.publishEvent(workflow);
    }
}
