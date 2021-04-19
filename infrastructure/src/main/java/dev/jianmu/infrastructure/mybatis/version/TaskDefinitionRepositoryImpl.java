package dev.jianmu.infrastructure.mybatis.version;

import dev.jianmu.infrastructure.mapper.version.TaskDefinitionMapper;
import dev.jianmu.version.aggregate.TaskDefinition;
import dev.jianmu.version.repository.TaskDefinitionRepository;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

/**
 * @class: TaskDefinitionRepositoryImpl
 * @description: 任务定义仓储实现
 * @author: Ethan Liu
 * @create: 2021-04-18 16:03
 **/
@Repository
public class TaskDefinitionRepositoryImpl implements TaskDefinitionRepository {
    private final TaskDefinitionMapper taskDefinitionMapper;

    @Inject
    public TaskDefinitionRepositoryImpl(TaskDefinitionMapper taskDefinitionMapper) {
        this.taskDefinitionMapper = taskDefinitionMapper;
    }

    @Override
    public void add(TaskDefinition taskDefinition) {
        this.taskDefinitionMapper.add(taskDefinition);
    }

    @Override
    public Optional<TaskDefinition> findByRef(String ref) {
        return this.taskDefinitionMapper.findByRef(ref);
    }

    @Override
    public void delete(TaskDefinition taskDefinition) {
        this.taskDefinitionMapper.delete(taskDefinition);
    }

    @Override
    public void updateName(TaskDefinition taskDefinition) {
        this.taskDefinitionMapper.updateName(taskDefinition);
    }

    @Override
    public List<TaskDefinition> findAll(int pageNum, int pageSize) {
        return this.taskDefinitionMapper.findAll(pageNum, pageSize);
    }
}
