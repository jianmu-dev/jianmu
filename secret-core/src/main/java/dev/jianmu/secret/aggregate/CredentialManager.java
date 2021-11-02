package dev.jianmu.secret.aggregate;

import java.util.List;
import java.util.Optional;

/**
 * @class: CredentialManager
 * @description: CredentialManager
 * @author: Ethan Liu
 * @create: 2021-11-01 21:15
 **/
public interface CredentialManager {

    String getType();

    void createNamespace(Namespace namespace);

    void deleteNamespace(String name);

    void createKVPair(KVPair kvPair);

    void deleteKVPair(String namespaceName, String key);

    Optional<Namespace> findNamespaceByName(String name);

    List<KVPair> findAllKVByNamespaceName(String namespaceName);

    List<Namespace> findAllNamespace();

    Optional<KVPair> findByNamespaceNameAndKey(String namespaceName, String key);
}
