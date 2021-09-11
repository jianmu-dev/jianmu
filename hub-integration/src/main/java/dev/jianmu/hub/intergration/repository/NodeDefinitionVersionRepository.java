package dev.jianmu.hub.intergration.repository;

import dev.jianmu.hub.intergration.aggregate.NodeDefinitionVersion;

import java.util.Optional;

/**
 * @class: NodeDefinitionVersionRepository
 * @description: NodeDefinitionVersionRepository
 * @author: Ethan Liu
 * @create: 2021-09-03 20:44
 **/
public interface NodeDefinitionVersionRepository {
    Optional<NodeDefinitionVersion> findByRefAndVersion(String ref, String version);

    void saveOrUpdate(NodeDefinitionVersion nodeDefinitionVersion);
}
