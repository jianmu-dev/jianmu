package dev.jianmu.infrastructure.credential;

import dev.jianmu.secret.aggregate.CredentialManager;
import dev.jianmu.secret.aggregate.KVPair;
import dev.jianmu.secret.aggregate.Namespace;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.vault.core.VaultKeyValueOperationsSupport;
import org.springframework.vault.core.VaultOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Ethan Liu
 * @class VaultCredentialManager
 * @description VaultCredentialManager
 * @create 2021-11-02 06:42
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "credential", name = "type", havingValue = "vault")
public class VaultCredentialManager implements CredentialManager {
    private final static String EXAMPLE_KEY = "JIANMU_EXAMPLE_KEY";
    private final static String EXAMPLE_VALUE = "JIANMU_EXAMPLE_VALUE";
    private final VaultOperations vaultOperations;
    private final CredentialProperties credentialProperties;

    public VaultCredentialManager(VaultOperations vaultOperations, CredentialProperties credentialProperties) {
        this.vaultOperations = vaultOperations;
        this.credentialProperties = credentialProperties;
    }

    @Override
    public String getType() {
        return "vault";
    }

    @Override
    public void createNamespace(Namespace namespace) {
        var name = this.getNamespace(namespace.getAssociationId(), namespace.getAssociationType(), namespace.getName(), namespace.getAssociationPlatform());
        var res = this.vaultOperations.opsForKeyValue(this.credentialProperties.getVault().getVaultEngineName(), VaultKeyValueOperationsSupport.KeyValueBackend.KV_1)
                .get(name);
        if (res != null && res.getData() != null) {
            throw new RuntimeException("该命名空间已存在");
        }
        var map = Map.of(EXAMPLE_KEY, EXAMPLE_VALUE);
        this.vaultOperations.opsForKeyValue(this.credentialProperties.getVault().getVaultEngineName(), VaultKeyValueOperationsSupport.KeyValueBackend.KV_1)
                .put(name, map);
    }

    @Override
    public void deleteNamespace(String associationId, String associationType, String associationPlatform, String name) {
        this.vaultOperations.opsForKeyValue(this.credentialProperties.getVault().getVaultEngineName(), VaultKeyValueOperationsSupport.KeyValueBackend.KV_1)
                .delete(this.getNamespace(associationId, associationType, associationPlatform, name));
    }

    @Override
    public void createKVPair(KVPair kvPair) {
        var namespace = this.getNamespace(kvPair.getAssociationId(), kvPair.getAssociationType(), kvPair.getAssociationPlatform(), kvPair.getNamespaceName());
        if (kvPair.getKey().equals(EXAMPLE_KEY)) {
            throw new RuntimeException("该密钥名称为内置密钥名称，不可添加");
        }
        var res = this.vaultOperations.opsForKeyValue(this.credentialProperties.getVault().getVaultEngineName(), VaultKeyValueOperationsSupport.KeyValueBackend.KV_1)
                .get(namespace);
        if (res == null || res.getData() == null) {
            throw new RuntimeException("未找到对应的命名空间");
        }
        if (res.getData().get(kvPair.getKey()) != null) {
            throw new RuntimeException("密钥唯一标识在该命名空间下已存在");
        }
        res.getData().put(kvPair.getKey(), kvPair.getValue());
        this.vaultOperations.opsForKeyValue(this.credentialProperties.getVault().getVaultEngineName(), VaultKeyValueOperationsSupport.KeyValueBackend.KV_1)
                .put(namespace, res.getData());
    }

    @Override
    public void deleteKVPair(String associationId, String associationType, String associationPlatform, String namespaceName, String key) {
        var namespace = this.getNamespace(associationId, associationType, namespaceName, associationPlatform);
        if (key.equals(EXAMPLE_KEY)) {
            throw new RuntimeException("该密钥名称为内置密钥名称，不可删除");
        }
        var res = this.vaultOperations.opsForKeyValue(this.credentialProperties.getVault().getVaultEngineName(), VaultKeyValueOperationsSupport.KeyValueBackend.KV_1)
                .get(namespace);
        if (res == null || res.getData() == null) {
            throw new RuntimeException("未找到对应的命名空间");
        }
        res.getData().remove(key);
        if (res.getData().isEmpty()) {
            res.getData().put(EXAMPLE_KEY, EXAMPLE_VALUE);
        }
        this.vaultOperations.opsForKeyValue(this.credentialProperties.getVault().getVaultEngineName(), VaultKeyValueOperationsSupport.KeyValueBackend.KV_1)
                .put(namespace, res.getData());
    }

    @Override
    public Optional<Namespace> findNamespaceByName(String associationId, String associationType, String associationPlatform, String name) {
        var namespace = this.getNamespace(associationId, associationType, associationPlatform, name);
        var res = this.vaultOperations.opsForKeyValue(this.credentialProperties.getVault().getVaultEngineName(), VaultKeyValueOperationsSupport.KeyValueBackend.KV_1)
                .get(namespace);
        if (res == null) {
            return Optional.empty();
        }
        return Optional.of(Namespace.Builder.aNamespace().name(namespace).build());
    }

    @Override
    public List<KVPair> findAllKVByNamespaceName(String associationId, String associationType, String associationPlatform, String namespaceName) {
        var namespace = this.getNamespace(associationId, associationType, associationPlatform, namespaceName);
        List<KVPair> kvPairs = new ArrayList<>();
        var res = this.vaultOperations.opsForKeyValue(this.credentialProperties.getVault().getVaultEngineName(), VaultKeyValueOperationsSupport.KeyValueBackend.KV_1)
                .get(namespace);
        if (res != null && res.getData() != null) {
            kvPairs = res.getData().entrySet().stream()
                    .filter(entry -> !entry.getKey().equals(EXAMPLE_KEY))
                    .map(entry -> KVPair.Builder.aKVPair().namespaceName(namespace).key(entry.getKey()).value(entry.getValue().toString()).build())
                    .collect(Collectors.toList());
        }
        return kvPairs;
    }

    @Override
    public List<Namespace> findAllNamespace(String associationId, String associationType, String associationPlatform) {
        var path = this.credentialProperties.getVault().getVaultEngineName();
        if (associationId != null && associationType != null) {
            path = path + "/" + associationType + "/" + associationId + "/";
        }
        var list = vaultOperations.list(path);
        if (list == null) {
            return List.of();
        }
        return list.stream()
                .filter(name -> {
                    if (associationId != null && associationType != null) {
                        return true;
                    }
                    return !name.endsWith("/");
                })
                .map(name -> Namespace.Builder.aNamespace().name(name).build())
                .collect(Collectors.toList());

    }

    @Override
    public Optional<KVPair> findByNamespaceNameAndKey(String associationId, String associationType, String associationPlatform, String namespaceName, String key) {
        var namespace = this.getNamespace(associationId, associationType, associationPlatform, namespaceName);
        List<KVPair> kvPairs = new ArrayList<>();
        try {
            var res = this.vaultOperations.opsForKeyValue(this.credentialProperties.getVault().getVaultEngineName(), VaultKeyValueOperationsSupport.KeyValueBackend.KV_1)
                    .get(namespace);
            if (res != null && res.getData() != null) {
                return res.getData().entrySet().stream()
                        .filter(entry -> entry.getKey().equals(key) && !entry.getKey().equals(EXAMPLE_KEY))
                        .map(entry -> KVPair.Builder.aKVPair().namespaceName(namespaceName).key(entry.getKey()).value(entry.getValue().toString()).build())
                        .findFirst();
            }
        } catch (Exception e) {
            log.warn("vault exception: {}", e.getMessage());
        }
        return Optional.empty();
    }

    private String getNamespace(String associationId, String associationType, String namespaceName, String associationPlatform) {
        if (associationId != null && associationType != null && associationPlatform != null) {
            return associationPlatform + "/" + associationType + "/" + associationId + "/" + namespaceName;
        }
        return namespaceName;
    }

    @Override
    public void deleteByAssociationIdAndType(String associationId, String associationType, String associationPlatform) {
        this.findAllNamespace(associationId, associationType, associationPlatform)
                .forEach(namespace -> this.vaultOperations.opsForKeyValue(this.credentialProperties.getVault().getVaultEngineName(), VaultKeyValueOperationsSupport.KeyValueBackend.KV_1)
                .delete(this.getNamespace(associationId, associationType, associationPlatform, namespace.getName())));
    }
}
