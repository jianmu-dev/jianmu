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
 * @author Ethan Liu
 * @class LocalCredentialManager
 * @description LocalCredentialManager
 * @create 2021-11-02 06:35
 */
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
    public String getType() {
        return "local";
    }

    @Override
    public void createNamespace(Namespace namespace) {
        namespace.setLastModifiedTime();
        this.namespaceMapper.add(namespace);
    }

    @Override
    public void deleteNamespace(String associationId, String associationType, String associationPlatform, String name) {
        this.namespaceMapper.delete(associationId, associationType, associationPlatform, name);
        this.kvPairMapper.deleteByName(associationId, associationType, associationPlatform, name);
    }

    @Override
    public void createKVPair(KVPair kvPair) {
        var namespace = this.namespaceMapper.findByName(kvPair.getAssociationId(), kvPair.getAssociationType(), kvPair.getAssociationPlatform(), kvPair.getNamespaceName())
                .orElseThrow(() -> new RuntimeException("未找到对应的命名空间"));
        namespace.setLastModifiedTime();

        var existedKvPair = this.kvPairMapper.findByNamespaceNameAndKey(kvPair.getAssociationId(), kvPair.getAssociationType(), kvPair.getAssociationPlatform(), kvPair.getNamespaceName(), kvPair.getKey());
        if (existedKvPair.isPresent()) {
            throw new RuntimeException("密钥唯一标识在该命名空间下已存在");
        }

        this.namespaceMapper.updateLastModifiedTime(namespace);
        this.kvPairMapper.add(kvPair);
    }

    @Override
    public void deleteKVPair(String associationId, String associationType, String associationPlatform, String namespaceName, String key) {
        var namespace = this.namespaceMapper.findByName(associationId, associationType, associationPlatform, namespaceName)
                .orElseThrow(() -> new RuntimeException("未找到对应的命名空间"));
        namespace.setLastModifiedTime();
        this.namespaceMapper.updateLastModifiedTime(namespace);
        this.kvPairMapper.deleteByNameAndKey(associationId, associationType, associationPlatform, namespaceName, key);
    }

    @Override
    public Optional<Namespace> findNamespaceByName(String associationId, String associationType, String associationPlatform, String name) {
        return this.namespaceMapper.findByName(associationId, associationType, associationPlatform, name);
    }

    @Override
    public List<KVPair> findAllKVByNamespaceName(String associationId, String associationType, String associationPlatform, String namespaceName) {
        return this.kvPairMapper.findByNamespaceName(associationId, associationType, associationPlatform, namespaceName);
    }

    @Override
    public List<Namespace> findAllNamespace(String associationId, String associationType, String associationPlatform) {
        return this.namespaceMapper.findAll(associationId, associationType, associationPlatform);
    }

    @Override
    public Optional<KVPair> findByNamespaceNameAndKey(String associationId, String associationType, String associationPlatform, String namespaceName, String key) {
        return this.kvPairMapper.findByNamespaceNameAndKey(associationId, associationType, associationPlatform, namespaceName, key);
    }

    @Override
    public void deleteByAssociationIdAndType(String associationId, String associationType, String associationPlatform) {
        this.namespaceMapper.deleteByAssociationIdAndType(associationId, associationType, associationPlatform);
        this.kvPairMapper.deleteByAssociationIdAndType(associationId, associationType, associationPlatform);
    }
}
