package dev.jianmu.project.repository;

import dev.jianmu.project.aggregate.ProjectLastExecution;

import java.util.Optional;

/**
 * @class ProjectLastExecutionRepository
 * @description ProjectLastExecutionRepository
 * @author Daihw
 * @create 2022/7/18 4:06 下午
 */
public interface ProjectLastExecutionRepository {
    void add(ProjectLastExecution projectLastExecution);

    void update(ProjectLastExecution projectLastExecution);

    void deleteByRef(String workflowRef);

    Optional<ProjectLastExecution> findByRef(String workflowRef);
}
