package dev.jianmu.infrastructure.credential;

import dev.jianmu.secret.aggregate.CredentialManager;
import dev.jianmu.secret.aggregate.KVPair;
import dev.jianmu.secret.aggregate.Namespace;
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
 * @class: VaultCredentialManager
 * @description: VaultCredentialManager
 * @author: Ethan Liu
 * @create: 2021-11-02 06:42
 **/
@Component
@ConditionalOnProperty(prefix = "credential", name = "type", havingValue = "vault")
public class VaultCredentialManager implements CredentialManager {
    private final VaultOperations vaultOperations;
    private final CredentialProperties credentialProperties;

    private final static String EXAMPLE_KEY = "JIANMU_EXAMPLE_KEY";
    private final static String EXAMPLE_VALUE = "JIANMU_EXAMPLE_VALUE";

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
        var res = this.vaultOperations.opsForKeyValue(this.credentialProperties.getVault().getVaultEngineName(), VaultKeyValueOperationsSupport.KeyValueBackend.KV_1)
                .get(namespace.getName());
        if (res != null && res.getData() != null) {
            throw new RuntimeException("该命名空间已存在");
        }
        var map = Map.of(EXAMPLE_KEY, EXAMPLE_VALUE);
        this.vaultOperations.opsForKeyValue(this.credentialProperties.getVault().getVaultEngineName(), VaultKeyValueOperationsSupport.KeyValueBackend.KV_1)
                .put(namespace.getName(), map);
    }

    @Override
    public void deleteNamespace(String name) {
        this.vaultOperations.opsForKeyValue(this.credentialProperties.getVault().getVaultEngineName(), VaultKeyValueOperationsSupport.KeyValueBackend.KV_1)
                .delete(name);
    }

    @Override
    public void createKVPair(KVPair kvPair) {
        if (kvPair.getKey().equals(EXAMPLE_KEY)) {
            throw new RuntimeException("该密钥名称为内置密钥名称，不可添加");
        }
        var res = this.vaultOperations.opsForKeyValue(this.credentialProperties.getVault().getVaultEngineName(), VaultKeyValueOperationsSupport.KeyValueBackend.KV_1)
                .get(kvPair.getNamespaceName());
        if (res == null || res.getData() == null) {
            throw new RuntimeException("未找到对应的命名空间");
        }
        if (res.getData().get(kvPair.getKey()) != null) {
            throw new RuntimeException("秘钥名称在该命名空间下已存在");
        }
        res.getData().put(kvPair.getKey(), kvPair.getValue());
        this.vaultOperations.opsForKeyValue(this.credentialProperties.getVault().getVaultEngineName(), VaultKeyValueOperationsSupport.KeyValueBackend.KV_1)
                .put(kvPair.getNamespaceName(), res.getData());
    }

    @Override
    public void deleteKVPair(String namespaceName, String key) {
        if (key.equals(EXAMPLE_KEY)) {
            throw new RuntimeException("该密钥名称为内置密钥名称，不可删除");
        }
        var res = this.vaultOperations.opsForKeyValue(this.credentialProperties.getVault().getVaultEngineName(), VaultKeyValueOperationsSupport.KeyValueBackend.KV_1)
                .get(namespaceName);
        if (res == null || res.getData() == null) {
            throw new RuntimeException("未找到对应的命名空间");
        }
        res.getData().remove(key);
        if (res.getData().isEmpty()) {
            res.getData().put(EXAMPLE_KEY, EXAMPLE_VALUE);
        }
        this.vaultOperations.opsForKeyValue(this.credentialProperties.getVault().getVaultEngineName(), VaultKeyValueOperationsSupport.KeyValueBackend.KV_1)
                .put(namespaceName, res.getData());
    }

    @Override
    public Optional<Namespace> findNamespaceByName(String name) {
        var res = this.vaultOperations.opsForKeyValue(this.credentialProperties.getVault().getVaultEngineName(), VaultKeyValueOperationsSupport.KeyValueBackend.KV_1)
                .get(name);
        if (res == null) {
            return Optional.empty();
        }
        return Optional.of(Namespace.Builder.aNamespace().name(name).build());
    }

    @Override
    public List<KVPair> findAllKVByNamespaceName(String namespaceName) {
        List<KVPair> kvPairs = new ArrayList<>();
        var res = this.vaultOperations.opsForKeyValue(this.credentialProperties.getVault().getVaultEngineName(), VaultKeyValueOperationsSupport.KeyValueBackend.KV_1)
                .get(namespaceName);
        if (res != null && res.getData() != null) {
            kvPairs = res.getData().entrySet().stream()
                    .filter(entry -> !entry.getKey().equals(EXAMPLE_KEY))
                    .map(entry -> KVPair.Builder.aKVPair().namespaceName(namespaceName).key(entry.getKey()).value(entry.getValue().toString()).build())
                    .collect(Collectors.toList());
        }
        return kvPairs;
    }

    @Override
    public List<Namespace> findAllNamespace() {
        var list = vaultOperations.list(this.credentialProperties.getVault().getVaultEngineName());
        if (list == null) {
            return List.of();
        }
        return list.stream()
                .map(name -> Namespace.Builder.aNamespace().name(name).build())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<KVPair> findByNamespaceNameAndKey(String namespaceName, String key) {
        List<KVPair> kvPairs = new ArrayList<>();
        var res = this.vaultOperations.opsForKeyValue(this.credentialProperties.getVault().getVaultEngineName(), VaultKeyValueOperationsSupport.KeyValueBackend.KV_1)
                .get(namespaceName);
        if (res != null && res.getData() != null) {
            return res.getData().entrySet().stream()
                    .filter(entry -> entry.getKey().equals(key) && !entry.getKey().equals(EXAMPLE_KEY))
                    .map(entry -> KVPair.Builder.aKVPair().namespaceName(namespaceName).key(entry.getKey()).value(entry.getValue().toString()).build())
                    .findFirst();
        }
        return Optional.empty();
    }
}
