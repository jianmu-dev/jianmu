package dev.jianmu.infrastructure.mybatis.project;

import dev.jianmu.infrastructure.mapper.project.TrashProjectMapper;
import dev.jianmu.project.aggregate.Project;
import dev.jianmu.project.repository.TrashProjectRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @class TrashProjectRepositoryImpl
 * @description TrashProjectRepositoryImpl
 * @author Daihw
 * @create 2023/5/22 10:33 上午
 */
@Repository
public class TrashProjectRepositoryImpl implements TrashProjectRepository {
    private final TrashProjectMapper trashProjectMapper;

    public TrashProjectRepositoryImpl(TrashProjectMapper trashProjectMapper) {
        this.trashProjectMapper = trashProjectMapper;
    }

    @Override
    public void add(Project project) {
        this.trashProjectMapper.add(project);
    }

    @Override
    public void deleteByWorkflowRef(String workflowRef) {
        this.trashProjectMapper.deleteByWorkflowRef(workflowRef);
    }

    @Override
    public Optional<Project> findById(String id) {
        return this.trashProjectMapper.findById(id);
    }
}
