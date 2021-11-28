package dev.jianmu.node.definition.repository;

import dev.jianmu.node.definition.aggregate.NodeDefinitionVersion;

import java.util.List;
import java.util.Optional;

/**
 * @class NodeDefinitionVersionRepository
 * @description NodeDefinitionVersionRepository
 * @author Ethan Liu
 * @create 2021-09-03 20:44
*/
public interface NodeDefinitionVersionRepository {
    Optional<NodeDefinitionVersion> findByOwnerRefAndRefAndVersion(String ownerRef, String ref, String version);

    List<NodeDefinitionVersion> findByOwnerRefAndRef(String ownerRef, String ref);

    void saveOrUpdate(NodeDefinitionVersion nodeDefinitionVersion);

    void deleteByOwnerRefAndRef(String ownerRef, String ref);
}
