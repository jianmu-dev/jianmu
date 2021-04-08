package dev.jianmu.infrastructure.repositoryimpl.task;

import dev.jianmu.infrastructure.mapper.task.TaskDefinitionMapper;
import dev.jianmu.task.aggregate.TaskDefinition;
import dev.jianmu.task.aggregate.EnvType;
import dev.jianmu.task.repository.TaskDefinitionRepository;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

/**
 * @class: DefinitionRepositoryImpl
 * @description: 任务定义仓储实现类
 * @author: Ethan Liu
 * @create: 2021-03-25 21:00
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
    public Optional<TaskDefinition> findByKeyAndVersion(String key, String version) {
        return this.taskDefinitionMapper.findByKeyVersion(key + version);
    }

    @Override
    public Optional<TaskDefinition> findByKeyVersion(String keyVersion) {
        return this.taskDefinitionMapper.findByKeyVersion(keyVersion);
    }

    @Override
    public List<TaskDefinition> findByKey(String key) {
        return this.taskDefinitionMapper.findByKey(key);
    }

    @Override
    public List<TaskDefinition> findAll() {
        return this.taskDefinitionMapper.findAll();
    }
}
