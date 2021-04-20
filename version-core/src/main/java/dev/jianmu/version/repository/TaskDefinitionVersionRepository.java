package dev.jianmu.version.repository;

import dev.jianmu.version.aggregate.TaskDefinitionVersion;

import java.util.List;
import java.util.Optional;

/**
 * @class: TaskDefinitionVersionRepository
 * @description: 任务定义版本仓储接口
 * @author: Ethan Liu
 * @create: 2021-04-18 09:52
 **/
public interface TaskDefinitionVersionRepository {
    void add(TaskDefinitionVersion taskDefinitionVersion);

    void delete(TaskDefinitionVersion taskDefinitionVersion);

    void updateDescription(TaskDefinitionVersion taskDefinitionVersion);

    List<TaskDefinitionVersion> findByTaskDefinitionRef(String taskDefinitionRef);

    Optional<TaskDefinitionVersion> findByTaskDefinitionRefAndName(String taskDefinitionRef, String name);

    Optional<TaskDefinitionVersion> findByDefinitionKey(String key);
}
