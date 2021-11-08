package dev.jianmu.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import dev.jianmu.node.definition.aggregate.NodeDefinition;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @class: NodeDefinitionDto
 * @description: NodeDefinitionDto
 * @author: Ethan Liu
 * @create: 2021-09-08 19:48
 **/
@NoArgsConstructor
@Data
public class NodeDefinitionDto {
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private String icon;
    private String name;
    private String ownerName;
    private String ownerType;
    private String ownerRef;
    private String creatorName;
    private String creatorRef;
    private NodeDefinition.Type type;
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private String description;
    private String ref;
    private String sourceLink;
    private String documentLink;
}
