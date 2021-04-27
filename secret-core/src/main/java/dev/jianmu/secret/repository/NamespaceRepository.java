package dev.jianmu.secret.repository;

import dev.jianmu.secret.aggregate.Namespace;

import java.util.Optional;

/**
 * @class: NamespaceRepository
 * @description: 命名空间仓储接口
 * @author: Ethan Liu
 * @create: 2021-04-20 12:45
 **/
public interface NamespaceRepository {
    void add(Namespace namespace);

    void delete(String name);

    Optional<Namespace> findByName(String name);

    void updateLastModifiedTime(Namespace namespace);
}
