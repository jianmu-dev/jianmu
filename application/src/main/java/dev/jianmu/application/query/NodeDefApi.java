package dev.jianmu.application.query;

import dev.jianmu.hub.intergration.aggregate.NodeDef;
import dev.jianmu.hub.intergration.aggregate.NodeDefinitionVersion;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @class: NodeDefApi
 * @description: 节点定义查询API
 * @author: Ethan Liu
 * @create: 2021-09-04 18:31
 **/
public interface NodeDefApi {
    List<NodeDef> findByTypes(Set<String> types);

    Optional<NodeDefinitionVersion> findByType(String type);
}
