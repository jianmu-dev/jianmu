package dev.jianmu.application.query;

import java.util.List;
import java.util.Set;

/**
 * @class: NodeDefApi
 * @description: 节点定义查询API
 * @author: Ethan Liu
 * @create: 2021-09-04 18:31
 **/
public interface NodeDefApi {
    List<NodeDef> findByTypes(Set<String> types);

    List<NodeDef> getByTypes(Set<String> types);

    NodeDef findByType(String type);

    NodeDef getByType(String type);
}
