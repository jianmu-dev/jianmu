package dev.jianmu.secret.aggregate;

import java.util.List;
import java.util.Optional;

/**
 * @class CredentialManager
 * @description CredentialManager
 * @author Ethan Liu
 * @create 2021-11-01 21:15
*/
public interface CredentialManager {

    String getType();

    void createNamespace(Namespace namespace);

    void deleteNamespace(String associationId, String associationType, String name);

    void createKVPair(KVPair kvPair);

    void deleteKVPair(String associationId, String associationType, String namespaceName, String key);

    Optional<Namespace> findNamespaceByName(String associationId, String associationType, String name);

    List<KVPair> findAllKVByNamespaceName(String associationId, String associationType, String namespaceName);

    List<Namespace> findAllNamespace(String associationId, String associationType);

    Optional<KVPair> findByNamespaceNameAndKey(String associationId, String associationType, String namespaceName, String key);

    void deleteByAssociationIdAndType(String id, String name);
}
