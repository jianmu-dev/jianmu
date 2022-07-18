package dev.jianmu.application.service;

import dev.jianmu.secret.aggregate.BaseAssociation;
import dev.jianmu.secret.aggregate.CredentialManager;
import dev.jianmu.secret.aggregate.KVPair;
import dev.jianmu.secret.aggregate.Namespace;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @class SecretApplication
 * @description 密钥管理应用服务
 * @author Ethan Liu
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

    public void deleteNamespace(String associationId, String associationType, String name) {
        this.credentialManager.deleteNamespace(associationId, associationType, name);
    }

    public void createKVPair(KVPair kvPair) {
        this.credentialManager.createKVPair(kvPair);
    }

    public void deleteKVPair(String associationId, String associationType, String namespaceName, String key) {
        this.credentialManager.deleteKVPair(associationId, associationType, namespaceName, key);
    }

    public Optional<Namespace> findByName(String associationId, String associationType, String name) {
        return this.credentialManager.findNamespaceByName(associationId, associationType, name);
    }

    public List<KVPair> findAllByNamespaceName(String associationId, String associationType, String namespaceName) {
        return this.credentialManager.findAllKVByNamespaceName(associationId, associationType, namespaceName);
    }

    public List<Namespace> findAll(String associationId, String associationType) {
        return this.credentialManager.findAllNamespace(associationId, associationType);
    }

    public String getCredentialManagerType() {
        return this.credentialManager.getType();
    }
}
