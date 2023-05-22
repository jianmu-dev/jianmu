package dev.jianmu.project.repository;

import java.util.Optional;

import dev.jianmu.project.aggregate.Project;

/**
 * @class TrashProjectRepository
 * @description TrashProjectRepository
 * @author Daihw
 * @create 2023/5/22 10:32 上午
 */
public interface TrashProjectRepository {
    void add(Project project);

    void deleteByWorkflowRef(String workflowRef);

    Optional<Project> findById(String id);
}
