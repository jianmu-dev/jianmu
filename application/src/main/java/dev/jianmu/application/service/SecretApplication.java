package dev.jianmu.application.service;

import com.github.pagehelper.PageInfo;
import dev.jianmu.infrastructure.mybatis.secret.NamespaceRepositoryImpl;
import dev.jianmu.secret.aggregate.KVPair;
import dev.jianmu.secret.aggregate.Namespace;
import dev.jianmu.secret.repository.KVPairRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @class: SecretApplication
 * @description: 密钥管理应用服务
 * @author: Ethan Liu
 * @create: 2021-04-19 19:55
 **/
@Service
public class SecretApplication {
    private final NamespaceRepositoryImpl namespaceRepository;
    private final KVPairRepository kvPairRepository;

    public SecretApplication(NamespaceRepositoryImpl namespaceRepository, KVPairRepository kvPairRepository) {
        this.namespaceRepository = namespaceRepository;
        this.kvPairRepository = kvPairRepository;
    }

    public void createNamespace(Namespace namespace) {
        this.namespaceRepository.add(namespace);
    }

    public void deleteNamespace(String name) {
        this.namespaceRepository.delete(name);
        this.kvPairRepository.delete(name);
    }

    public void createKVPair(KVPair kvPair) {
        this.kvPairRepository.add(kvPair);
    }

    public void deleteKVPair(String namespaceName, String key) {
        this.kvPairRepository.delete(namespaceName, key);
    }

    public List<KVPair> findAll(String namespaceName) {
        return this.kvPairRepository.findByNamespaceName(namespaceName);
    }

    public PageInfo<Namespace> findAll(int pageNum, int pageSize) {
        return this.namespaceRepository.findAll(pageNum, pageSize);
    }
}
