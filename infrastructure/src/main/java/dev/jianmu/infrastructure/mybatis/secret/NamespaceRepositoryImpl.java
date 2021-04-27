package dev.jianmu.infrastructure.mybatis.secret;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import dev.jianmu.infrastructure.mapper.secret.NamespaceMapper;
import dev.jianmu.secret.aggregate.Namespace;
import dev.jianmu.secret.repository.NamespaceRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @class: NamespaceRepositoryImpl
 * @description: 命名空间仓储接口实现
 * @author: Ethan Liu
 * @create: 2021-04-20 13:25
 **/
@Repository
public class NamespaceRepositoryImpl implements NamespaceRepository {
    private final NamespaceMapper namespaceMapper;

    public NamespaceRepositoryImpl(NamespaceMapper namespaceMapper) {
        this.namespaceMapper = namespaceMapper;
    }

    @Override
    public void add(Namespace namespace) {
        this.namespaceMapper.add(namespace);
    }

    @Override
    public void delete(String name) {
        this.namespaceMapper.delete(name);
    }

    @Override
    public void updateLastModifiedTime(Namespace namespace) {
        this.namespaceMapper.updateLastModifiedTime(namespace);
    }

    @Override
    public Optional<Namespace> findByName(String name) {
        return this.namespaceMapper.findByName(name);
    }

    public PageInfo<Namespace> findAll(String name, int pageNum, int pageSize) {
        return PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(() -> this.namespaceMapper.findAll(name));
    }
}
