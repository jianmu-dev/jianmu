package dev.jianmu.task.repository;

import dev.jianmu.task.aggregate.TaskDefinition;

import java.util.List;
import java.util.Optional;

/**
 * @class: DefinitionRepository
 * @description: 任务定义仓储接口
 * @author: Ethan Liu
 * @create: 2021-03-25 19:27
 **/
public interface TaskDefinitionRepository {
    void add(TaskDefinition taskDefinition);

    Optional<TaskDefinition> findByKeyAndVersion(String key, String version);

    Optional<TaskDefinition> findByKeyVersion(String keyVersion);

    List<TaskDefinition> findByKey(String key);

    List<TaskDefinition> findAll();
}
