package dev.jianmu.application.query;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.node.definition.aggregate.NodeParameter;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
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
    @JsonIgnore
    private final Set<NodeParameter> inputParameters;
    @JsonIgnore
    private final Set<NodeParameter> outputParameters;
    @JsonIgnore
    private final String resultFile;
    @JsonIgnore
    private final String spec;

    public Set<NodeParameter> matchedOutputParameters(Map<String, Object> parameterMap) {
        return outputParameters.stream()
                .filter(nodeParameter -> parameterMap.get(nodeParameter.getRef()) != null)
                .collect(Collectors.toSet());
    }

    public String toJsonString() {
        var mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            log.warn("节点定义Json序列化失败: {}", e.getMessage());
            throw new RuntimeException("节点定义Json序列化失败");
        }
    }
}
