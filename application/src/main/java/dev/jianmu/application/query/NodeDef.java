package dev.jianmu.application.query;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.application.exception.OutputParamNotFoundException;
import dev.jianmu.node.definition.aggregate.NodeParameter;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @class NodeDef
 * @description 节点定义
 * @author Ethan Liu
 * @create 2021-09-04 11:59
*/
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
    // Shell Node
    // 镜像名称
    @JsonIgnore
    private final String image;
    // 命令列表
    @JsonIgnore
    private final List<String> script;

    public Set<NodeParameter> matchedOutputParameters(Map<String, Object> parameterMap) {
        var nodeParameters = new HashSet<NodeParameter>();
        outputParameters.forEach(nodeParameter -> {
            if (nodeParameter.getRequired() && parameterMap.get(nodeParameter.getRef()) == null) {
                throw new OutputParamNotFoundException(name + "节点的必填输出参数" + nodeParameter.getRef() + "为空");
            }
            if (parameterMap.get(nodeParameter.getRef()) != null) {
                nodeParameters.add(nodeParameter);
            }
        });
        return nodeParameters;
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
