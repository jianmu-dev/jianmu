package dev.jianmu.task.repository;

import dev.jianmu.task.aggregate.Definition;

import java.util.List;
import java.util.Optional;

/**
 * @class: DefinitionRepository
 * @description: 任务定义仓储接口
 * @author: Ethan Liu
 * @create: 2021-03-25 19:27
 **/
public interface DefinitionRepository {
    void add(Definition definition);

    List<Definition> findByRef(String ref);

    Optional<Definition> findByRefAndVersion(String refVersion);

    Optional<Definition> findByRefAndVersion(String ref, String version);

    void delete(String key);
}
