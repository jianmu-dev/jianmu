package dev.jianmu.infrastructure.mybatis.project;

import dev.jianmu.infrastructure.mapper.project.ProjectLastExecutionMapper;
import dev.jianmu.project.aggregate.ProjectLastExecution;
import dev.jianmu.project.repository.ProjectLastExecutionRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @class ProjectLastExecutionRepositoryImpl
 * @description ProjectLastExecutionRepositoryImpl
 * @author Daihw
 * @create 2022/7/18 4:08 下午
 */
@Repository
public class ProjectLastExecutionRepositoryImpl implements ProjectLastExecutionRepository {
    private final ProjectLastExecutionMapper projectLastExecutionMapper;

    public ProjectLastExecutionRepositoryImpl(ProjectLastExecutionMapper projectLastExecutionMapper) {
        this.projectLastExecutionMapper = projectLastExecutionMapper;
    }

    @Override
    public void add(ProjectLastExecution projectLastExecution) {
        this.projectLastExecutionMapper.add(projectLastExecution);
    }

    @Override
    public void update(ProjectLastExecution projectLastExecution) {
        this.projectLastExecutionMapper.update(projectLastExecution);
    }

    @Override
    public void deleteByRef(String workflowRef) {
        this.projectLastExecutionMapper.deleteByRef(workflowRef);
    }

    @Override
    public Optional<ProjectLastExecution> findByRef(String workflowRef) {
        return this.projectLastExecutionMapper.findByRef(workflowRef);
    }

}
