package dev.jianmu.application.service;

import dev.jianmu.secret.aggregate.CredentialManager;
import dev.jianmu.secret.aggregate.KVPair;
import dev.jianmu.secret.aggregate.Namespace;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @class: SecretApplication
 * @description: 密钥管理应用服务
 * @author: Ethan Liu
 * @create: 2021-04-19 19:55
 **/
@Service
public class SecretApplication {
    private final CredentialManager credentialManager;

    public SecretApplication(CredentialManager credentialManager) {
        this.credentialManager = credentialManager;
    }

    public void createNamespace(Namespace namespace) {
        this.credentialManager.createNamespace(namespace);
    }

    public void deleteNamespace(String name) {
        this.credentialManager.deleteNamespace(name);
    }

    public void createKVPair(KVPair kvPair) {
        this.credentialManager.createKVPair(kvPair);
    }

    public void deleteKVPair(String namespaceName, String key) {
        this.credentialManager.deleteKVPair(namespaceName, key);
    }

    public Optional<Namespace> findByName(String name) {
        return this.credentialManager.findNamespaceByName(name);
    }

    public List<KVPair> findAllByNamespaceName(String namespaceName) {
        return this.credentialManager.findAllKVByNamespaceName(namespaceName);
    }

    public List<Namespace> findAll() {
        return this.credentialManager.findAllNamespace();
    }
}
