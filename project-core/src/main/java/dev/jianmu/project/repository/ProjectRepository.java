package dev.jianmu.project.repository;


import dev.jianmu.project.aggregate.Project;

import java.util.Optional;

/**
 * @class: DslReferenceRepository
 * @description: DSL流程定义关联仓储
 * @author: Ethan Liu
 * @create: 2021-04-23 11:18
 **/
public interface ProjectRepository {
    void add(Project project);

    void deleteByWorkflowRef(String workflowRef);

    void updateByWorkflowRef(Project project);

    Optional<Project> findById(String id);

    Optional<Project> findByIdAndWebhook(String id, String webhook);

    Optional<Project> findByWorkflowRef(String workflowRef);
}
