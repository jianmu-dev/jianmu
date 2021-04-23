package dev.jianmu.infrastructure.mybatis.dsl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import dev.jianmu.dsl.aggregate.DslReference;
import dev.jianmu.dsl.repository.DslReferenceRepository;
import dev.jianmu.infrastructure.mapper.dsl.DslReferenceMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @class: DslReferenceRepositoryImpl
 * @description: DSL流程定义关联仓储实现
 * @author: Ethan Liu
 * @create: 2021-04-23 11:39
 **/
@Repository
public class DslReferenceRepositoryImpl implements DslReferenceRepository {
    private final DslReferenceMapper dslReferenceMapper;

    public DslReferenceRepositoryImpl(DslReferenceMapper dslReferenceMapper) {
        this.dslReferenceMapper = dslReferenceMapper;
    }

    @Override
    public void add(DslReference dslReference) {
        this.dslReferenceMapper.add(dslReference);
    }

    @Override
    public void deleteByWorkflowRef(String workflowRef) {
        this.dslReferenceMapper.deleteByWorkflowRef(workflowRef);
    }

    @Override
    public void updateByWorkflowRef(DslReference dslReference) {
        this.dslReferenceMapper.updateByWorkflowRef(dslReference);
    }

    @Override
    public Optional<DslReference> findById(String id) {
        return this.dslReferenceMapper.findById(id);
    }

    @Override
    public Optional<DslReference> findByWorkflowRef(String workflowRef) {
        return this.dslReferenceMapper.findByWorkflowRef(workflowRef);
    }

    public PageInfo<DslReference> findAll(int pageNum, int pageSize) {
        return PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(this.dslReferenceMapper::findAll);
    }
}
