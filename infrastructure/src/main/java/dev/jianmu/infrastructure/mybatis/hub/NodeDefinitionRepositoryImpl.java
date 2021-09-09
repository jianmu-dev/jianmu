package dev.jianmu.infrastructure.mybatis.hub;

import dev.jianmu.hub.intergration.aggregate.NodeDefinition;
import dev.jianmu.hub.intergration.repository.NodeDefinitionRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @class: NodeDefinitionRepositoryImpl
 * @description: NodeDefinitionRepositoryImpl
 * @author: Ethan Liu
 * @create: 2021-09-08 21:51
 **/
@Repository
public class NodeDefinitionRepositoryImpl implements NodeDefinitionRepository {
    @Override
    public Optional<NodeDefinition> findById(String id) {
        return Optional.empty();
    }

    @Override
    public void saveOrUpdate(NodeDefinition nodeDefinition) {

    }
}
