package dev.jianmu.api.vo;

import dev.jianmu.node.definition.aggregate.NodeDefinition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @class NodeDefVo
 * @description NodeDefVo
 * @author Ethan Liu
 * @create 2021-09-15 20:58
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "节点定义VO")
public class NodeDefVo {
    private String icon;
    private String name;
    private String ownerName;
    private String ownerType;
    private String ownerRef;
    private String creatorName;
    private String creatorRef;
    private NodeDefinition.Type type;
    private String description;
    private String ref;
    private String sourceLink;
    private String documentLink;
    private List<String> versions;
    private Boolean deprecated;
}
