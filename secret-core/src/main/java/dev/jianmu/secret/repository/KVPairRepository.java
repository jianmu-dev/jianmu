package dev.jianmu.secret.repository;

import dev.jianmu.secret.aggregate.KVPair;

import java.util.List;
import java.util.Optional;

/**
 * @class: KVPairRepository
 * @description: 键值对仓储接口
 * @author: Ethan Liu
 * @create: 2021-04-20 12:45
 **/
public interface KVPairRepository {
    void add(KVPair kvPair);

    void delete(String namespaceName, String key);

    void delete(String namespaceName);

    Optional<KVPair> findByNamespaceNameAndKey(String namespaceName, String key);

    List<KVPair> findByNamespaceName(String namespaceName);
}
