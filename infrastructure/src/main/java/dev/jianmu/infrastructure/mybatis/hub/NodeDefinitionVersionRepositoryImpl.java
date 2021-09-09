package dev.jianmu.infrastructure.mybatis.hub;

import dev.jianmu.hub.intergration.aggregate.NodeDefinitionVersion;
import dev.jianmu.hub.intergration.repository.NodeDefinitionVersionRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @class: NodeDefinitionVersionRepositoryImpl
 * @description: NodeDefinitionVersionRepositoryImpl
 * @author: Ethan Liu
 * @create: 2021-09-08 21:52
 **/
@Repository
public class NodeDefinitionVersionRepositoryImpl implements NodeDefinitionVersionRepository {
    @Override
    public Optional<NodeDefinitionVersion> findByRefAndVersion(String ref, String version) {
        return Optional.empty();
    }
}
