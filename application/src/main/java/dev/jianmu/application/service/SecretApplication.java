package dev.jianmu.application.service;

import com.github.pagehelper.PageInfo;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.exception.RepeatFoundException;
import dev.jianmu.infrastructure.mybatis.secret.NamespaceRepositoryImpl;
import dev.jianmu.secret.aggregate.KVPair;
import dev.jianmu.secret.aggregate.Namespace;
import dev.jianmu.secret.repository.KVPairRepository;
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
    private final NamespaceRepositoryImpl namespaceRepository;
    private final KVPairRepository kvPairRepository;

    public SecretApplication(NamespaceRepositoryImpl namespaceRepository, KVPairRepository kvPairRepository) {
        this.namespaceRepository = namespaceRepository;
        this.kvPairRepository = kvPairRepository;
    }

    public void createNamespace(Namespace namespace) {
        namespace.setLastModifiedTime();
        this.namespaceRepository.add(namespace);
    }

    public void deleteNamespace(String name) {
        this.namespaceRepository.delete(name);
        this.kvPairRepository.delete(name);
    }

    public void createKVPair(KVPair kvPair) {
        var namespace = this.namespaceRepository.findByName(kvPair.getNamespaceName())
                .orElseThrow(() -> new DataNotFoundException("未找到对应的命名空间"));
        namespace.setLastModifiedTime();

        var existedKvPair = this.kvPairRepository.findByNamespaceNameAndKey(kvPair.getNamespaceName(), kvPair.getKey());
        if (existedKvPair.isPresent()) {
            throw new RepeatFoundException("秘钥名称在该命名空间下已存在");
        }

        this.namespaceRepository.updateLastModifiedTime(namespace);
        this.kvPairRepository.add(kvPair);
    }

    public void deleteKVPair(String namespaceName, String key) {
        var namespace = this.namespaceRepository.findByName(namespaceName)
                .orElseThrow(() -> new DataNotFoundException("未找到对应的命名空间"));
        namespace.setLastModifiedTime();
        this.namespaceRepository.updateLastModifiedTime(namespace);
        this.kvPairRepository.delete(namespaceName, key);
    }

    public Optional<Namespace> findById(String name) {
        return this.namespaceRepository.findByName(name);
    }

    public List<KVPair> findAll(String namespaceName) {
        return this.kvPairRepository.findByNamespaceName(namespaceName);
    }

    public PageInfo<Namespace> findAll(String name, int pageNum, int pageSize) {
        return this.namespaceRepository.findAll(name, pageNum, pageSize);
    }
}
