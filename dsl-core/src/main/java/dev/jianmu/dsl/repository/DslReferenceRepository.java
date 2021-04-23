package dev.jianmu.dsl.repository;

import dev.jianmu.dsl.aggregate.DslReference;

import java.util.Optional;

/**
 * @class: DslReferenceRepository
 * @description: DSL流程定义关联仓储
 * @author: Ethan Liu
 * @create: 2021-04-23 11:18
 **/
public interface DslReferenceRepository {
    void add(DslReference dslReference);

    void deleteByWorkflowRef(String workflowRef);

    void updateByWorkflowRef(DslReference dslReference);

    Optional<DslReference> findById(String id);

    Optional<DslReference> findByWorkflowRef(String workflowRef);
}
