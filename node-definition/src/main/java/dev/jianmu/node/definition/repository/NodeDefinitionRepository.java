package dev.jianmu.node.definition.repository;

import dev.jianmu.node.definition.aggregate.NodeDefinition;

import java.util.Optional;

/**
 * @class NodeDefinitionRepository
 * @description NodeDefinitionRepository
 * @author Ethan Liu
 * @create 2021-09-03 20:44
*/
public interface NodeDefinitionRepository {
    Optional<NodeDefinition> findById(String id);

    void saveOrUpdate(NodeDefinition nodeDefinition);

    void deleteById(String id);
}
