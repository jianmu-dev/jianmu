package dev.jianmu.infrastructure.mybatis.secret;

import dev.jianmu.infrastructure.mapper.secret.KVPairMapper;
import dev.jianmu.secret.aggregate.KVPair;
import dev.jianmu.secret.repository.KVPairRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @class: KVPairRepositoryImpl
 * @description: 键值对仓储接口实现
 * @author: Ethan Liu
 * @create: 2021-04-20 13:26
 **/
@Repository
public class KVPairRepositoryImpl implements KVPairRepository {
    private final KVPairMapper kvPairMapper;

    public KVPairRepositoryImpl(KVPairMapper kvPairMapper) {
        this.kvPairMapper = kvPairMapper;
    }

    @Override
    public void add(KVPair kvPair) {
        this.kvPairMapper.add(kvPair);
    }

    @Override
    public void delete(String namespaceName, String key) {
        this.kvPairMapper.deleteByNameAndKey(namespaceName, key);
    }

    @Override
    public void delete(String namespaceName) {
        this.kvPairMapper.deleteByName(namespaceName);
    }

    @Override
    public Optional<KVPair> findByKey(String key) {
        return this.kvPairMapper.findByKey(key);
    }

    @Override
    public List<KVPair> findByNamespaceName(String namespaceName) {
        return this.kvPairMapper.findByNamespaceName(namespaceName);
    }
}
