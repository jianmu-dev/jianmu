package dev.jianmu.application.query;

import dev.jianmu.hub.intergration.aggregate.NodeParameter;
import lombok.Builder;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

/**
 * @class: NodeDef
 * @description: 节点定义
 * @author: Ethan Liu
 * @create: 2021-09-04 11:59
 **/
@Getter
@Builder
public class NodeDef {
    private String name;
    private String description;
    private String type;
    private Set<NodeParameter> inputParameters = new HashSet<>();
    private Set<NodeParameter> outputParameters = new HashSet<>();
}
