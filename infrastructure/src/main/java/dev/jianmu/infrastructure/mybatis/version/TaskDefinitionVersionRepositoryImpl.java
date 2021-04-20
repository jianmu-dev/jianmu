package dev.jianmu.infrastructure.mybatis.version;

import dev.jianmu.infrastructure.mapper.version.TaskDefinitionVersionMapper;
import dev.jianmu.version.aggregate.TaskDefinitionVersion;
import dev.jianmu.version.repository.TaskDefinitionVersionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @class: TaskDefinitionVersionRepositoryImpl
 * @description: 任务定义版本仓储实现
 * @author: Ethan Liu
 * @create: 2021-04-18 16:04
 **/
@Repository
public class TaskDefinitionVersionRepositoryImpl implements TaskDefinitionVersionRepository {
    private final TaskDefinitionVersionMapper taskDefinitionVersionMapper;

    public TaskDefinitionVersionRepositoryImpl(TaskDefinitionVersionMapper taskDefinitionVersionMapper) {
        this.taskDefinitionVersionMapper = taskDefinitionVersionMapper;
    }

    @Override
    public void add(TaskDefinitionVersion taskDefinitionVersion) {
        this.taskDefinitionVersionMapper.add(taskDefinitionVersion);
    }

    @Override
    public void delete(TaskDefinitionVersion taskDefinitionVersion) {
        this.taskDefinitionVersionMapper.delete(taskDefinitionVersion);
    }

    @Override
    public void updateDescription(TaskDefinitionVersion taskDefinitionVersion) {
        this.taskDefinitionVersionMapper.updateDescription(taskDefinitionVersion);
    }

    @Override
    public List<TaskDefinitionVersion> findByTaskDefinitionRef(String taskDefinitionRef) {
        return this.taskDefinitionVersionMapper.findByTaskDefinitionRef(taskDefinitionRef);
    }

    @Override
    public Optional<TaskDefinitionVersion> findByTaskDefinitionRefAndName(String taskDefinitionRef, String name) {
        return this.taskDefinitionVersionMapper.findByTaskDefinitionRefAndName(taskDefinitionRef, name);
    }

    @Override
    public Optional<TaskDefinitionVersion> findByDefinitionKey(String key) {
        return this.taskDefinitionVersionMapper.findByDefinitionKey(key);
    }
}
