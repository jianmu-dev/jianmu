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
    private final String name;
    private final String description;
    private final String icon;
    private final String ownerName;
    private final String ownerType;
    private final String ownerRef;
    private final String creatorName;
    private final String creatorRef;
    private final String sourceLink;
    private final String documentLink;
    private final String type;
    private final String workerType;
    private final Set<NodeParameter> inputParameters;
    private final Set<NodeParameter> outputParameters;
    private final String resultFile;
    private final String spec;

    public Set<NodeParameter> matchedOutputParameters(Map<String, Object> parameterMap) {
        return outputParameters.stream()
                .filter(nodeParameter -> parameterMap.get(nodeParameter.getRef()) != null)
                .collect(Collectors.toSet());
    }
}
