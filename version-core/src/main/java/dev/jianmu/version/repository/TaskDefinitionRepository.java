package dev.jianmu.version.repository;

import dev.jianmu.version.aggregate.TaskDefinition;

import java.util.Optional;

/**
 * @class: TaskDefinitionRepository
 * @description: 任务定义仓储接口
 * @author: Ethan Liu
 * @create: 2021-04-18 09:51
 **/
public interface TaskDefinitionRepository {
    void add(TaskDefinition taskDefinition);

    Optional<TaskDefinition> findByRef(String ref);

    void delete(TaskDefinition taskDefinition);

    void updateName(TaskDefinition taskDefinition);
}
