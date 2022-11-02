package dev.jianmu.application.service;

import dev.jianmu.secret.aggregate.CredentialManager;
import dev.jianmu.secret.aggregate.KVPair;
import dev.jianmu.secret.aggregate.Namespace;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author Ethan Liu
 * @class SecretApplication
 * @description 密钥管理应用服务
 * @create 2021-04-19 19:55
 */
@Service
public class SecretApplication {
    private final CredentialManager credentialManager;

    public SecretApplication(CredentialManager credentialManager) {
        this.credentialManager = credentialManager;
    }

    public void createNamespace(Namespace namespace) {
        this.credentialManager.createNamespace(namespace);
    }

    public void deleteNamespace(String associationId, String associationType, String associationPlatform, String name) {
        this.credentialManager.deleteNamespace(associationId, associationType, associationPlatform, name);
    }

    public void createKVPair(KVPair kvPair) {
        this.credentialManager.createKVPair(kvPair);
    }

    public void deleteKVPair(String associationId, String associationType, String associationPlatform, String namespaceName, String key) {
        this.credentialManager.deleteKVPair(associationId, associationType, associationPlatform, namespaceName, key);
    }

    public Optional<Namespace> findByName(String associationId, String associationType, String associationPlatform, String name) {
        return this.credentialManager.findNamespaceByName(associationId, associationType, associationPlatform, name);
    }

    public List<KVPair> findAllByNamespaceName(String associationId, String associationType, String associationPlatform, String namespaceName) {
        return this.credentialManager.findAllKVByNamespaceName(associationId, associationType, associationPlatform, namespaceName);
    }

    public List<Namespace> findAll(String associationId, String associationType, String associationPlatform) {
        return this.credentialManager.findAllNamespace(associationId, associationType, associationPlatform);
    }

    public String getCredentialManagerType() {
        return this.credentialManager.getType();
    }
}
