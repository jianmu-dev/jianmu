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

    public VaultCredentialManager(VaultOperations vaultOperations, CredentialProperties credentialProperties) {
        this.vaultOperations = vaultOperations;
        this.credentialProperties = credentialProperties;
    }

    @Override
    public void createNamespace(Namespace namespace) {
    }

    @Override
    public void deleteNamespace(String name) {
    }

    @Override
    public void createKVPair(KVPair kvPair) {

    }

    @Override
    public void deleteKVPair(String namespaceName, String key) {

    }

    @Override
    public Optional<Namespace> findNamespaceByName(String name) {
        var res = this.vaultOperations.opsForKeyValue(this.credentialProperties.getPath(), VaultKeyValueOperationsSupport.KeyValueBackend.KV_1)
                .get(name);
        if (res == null) {
            return Optional.empty();
        }
        return Optional.of(Namespace.Builder.aNamespace().name(name).build());
    }

    @Override
    public List<KVPair> findAllKVByNamespaceName(String namespaceName) {
        List<KVPair> kvPairs = new ArrayList<>();
        var res = this.vaultOperations.opsForKeyValue(this.credentialProperties.getPath(), VaultKeyValueOperationsSupport.KeyValueBackend.KV_1)
                .get(namespaceName);
        if (res != null && res.getData() != null) {
            res.getData().forEach((k, v) ->
                    kvPairs.add(KVPair.Builder.aKVPair()
                            .namespaceName(namespaceName)
                            .key(k)
                            .value(v.toString())
                            .build())
            );
        }
        return kvPairs;
    }

    @Override
    public List<Namespace> findAllNamespace() {
        var list = vaultOperations.list(this.credentialProperties.getPath());
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
        var res = this.vaultOperations.opsForKeyValue(this.credentialProperties.getPath(), VaultKeyValueOperationsSupport.KeyValueBackend.KV_1)
                .get(namespaceName);
        if (res != null && res.getData() != null) {
            return res.getData().entrySet().stream()
                    .filter(entry -> entry.getKey().equals(key))
                    .map(entry -> KVPair.Builder.aKVPair().namespaceName(namespaceName).key(entry.getKey()).value(entry.getValue().toString()).build())
                    .findFirst();
        }
        return Optional.empty();
    }
}
