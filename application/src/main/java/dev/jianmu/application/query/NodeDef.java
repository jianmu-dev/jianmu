package dev.jianmu.application.query;

import dev.jianmu.hub.intergration.aggregate.NodeParameter;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private Set<NodeParameter> inputParameters;
    private Set<NodeParameter> outputParameters;
    private String resultFile;
    private String spec;

    public Set<NodeParameter> matchedOutputParameters(Map<String, Object> parameterMap) {
        return outputParameters.stream()
                .filter(taskParameter -> parameterMap.get(taskParameter.getRef()) != null)
                .collect(Collectors.toSet());
    }
}
