package dev.jianmu.infrastructure.credential;

import dev.jianmu.infrastructure.mapper.secret.KVPairMapper;
import dev.jianmu.infrastructure.mapper.secret.NamespaceMapper;
import dev.jianmu.secret.aggregate.CredentialManager;
import dev.jianmu.secret.aggregate.KVPair;
import dev.jianmu.secret.aggregate.Namespace;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * @class: LocalCredentialManager
 * @description: LocalCredentialManager
 * @author: Ethan Liu
 * @create: 2021-11-02 06:35
 **/
@Component
@ConditionalOnProperty(prefix = "credential", name = "type", havingValue = "local", matchIfMissing = true)
public class LocalCredentialManager implements CredentialManager {
    private final NamespaceMapper namespaceMapper;
    private final KVPairMapper kvPairMapper;

    public LocalCredentialManager(NamespaceMapper namespaceMapper, KVPairMapper kvPairMapper) {
        this.namespaceMapper = namespaceMapper;
        this.kvPairMapper = kvPairMapper;
    }

    @Override
    public void createNamespace(Namespace namespace) {
        namespace.setLastModifiedTime();
        this.namespaceMapper.add(namespace);
    }

    @Override
    public void deleteNamespace(String name) {
        this.namespaceMapper.delete(name);
        this.kvPairMapper.deleteByName(name);
    }

    @Override
    public void createKVPair(KVPair kvPair) {
        var namespace = this.namespaceMapper.findByName(kvPair.getNamespaceName())
                .orElseThrow(() -> new RuntimeException("未找到对应的命名空间"));
        namespace.setLastModifiedTime();

        var existedKvPair = this.kvPairMapper.findByNamespaceNameAndKey(kvPair.getNamespaceName(), kvPair.getKey());
        if (existedKvPair.isPresent()) {
            throw new RuntimeException("秘钥名称在该命名空间下已存在");
        }

        this.namespaceMapper.updateLastModifiedTime(namespace);
        this.kvPairMapper.add(kvPair);
    }

    @Override
    public void deleteKVPair(String namespaceName, String key) {
        var namespace = this.namespaceMapper.findByName(namespaceName)
                .orElseThrow(() -> new RuntimeException("未找到对应的命名空间"));
        namespace.setLastModifiedTime();
        this.namespaceMapper.updateLastModifiedTime(namespace);
        this.kvPairMapper.deleteByNameAndKey(namespaceName, key);
    }

    @Override
    public Optional<Namespace> findNamespaceByName(String name) {
        return this.namespaceMapper.findByName(name);
    }

    @Override
    public List<KVPair> findAllKVByNamespaceName(String namespaceName) {
        return this.kvPairMapper.findByNamespaceName(namespaceName);
    }

    @Override
    public List<Namespace> findAllNamespace() {
        return this.namespaceMapper.findAll();
    }

    @Override
    public Optional<KVPair> findByNamespaceNameAndKey(String namespaceName, String key) {
        return this.kvPairMapper.findByNamespaceNameAndKey(namespaceName, key);
    }
}
