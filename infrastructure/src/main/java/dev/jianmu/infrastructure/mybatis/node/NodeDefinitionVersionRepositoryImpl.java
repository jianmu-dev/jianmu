package dev.jianmu.infrastructure.mybatis.node;

import dev.jianmu.infrastructure.mapper.hub.NodeDefinitionVersionMapper;
import dev.jianmu.node.definition.aggregate.NodeDefinitionVersion;
import dev.jianmu.node.definition.repository.NodeDefinitionVersionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @class: NodeDefinitionVersionRepositoryImpl
 * @description: NodeDefinitionVersionRepositoryImpl
 * @author: Ethan Liu
 * @create: 2021-09-08 21:52
 **/
@Repository
public class NodeDefinitionVersionRepositoryImpl implements NodeDefinitionVersionRepository {
    private final NodeDefinitionVersionMapper nodeDefinitionVersionMapper;

    public NodeDefinitionVersionRepositoryImpl(NodeDefinitionVersionMapper nodeDefinitionVersionMapper) {
        this.nodeDefinitionVersionMapper = nodeDefinitionVersionMapper;
    }

    @Override
    public Optional<NodeDefinitionVersion> findByOwnerRefAndRefAndVersion(String ownerRef, String ref, String version) {
        return this.nodeDefinitionVersionMapper.findByOwnerRefAndRefAndVersion(ownerRef, ref, version);
    }

    @Override
    public List<NodeDefinitionVersion> findByOwnerRefAndRef(String ownerRef, String ref) {
        return this.nodeDefinitionVersionMapper.findByOwnerRefAndRef(ownerRef, ref);
    }

    @Override
    public void saveOrUpdate(NodeDefinitionVersion nodeDefinitionVersion) {
        this.nodeDefinitionVersionMapper.saveOrUpdate(nodeDefinitionVersion);
    }

    @Override
    public void deleteByOwnerRefAndRef(String ownerRef, String ref) {
        this.nodeDefinitionVersionMapper.deleteByOwnerRefAndRef(ownerRef, ref);
    }
}
