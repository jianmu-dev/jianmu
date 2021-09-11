package dev.jianmu.infrastructure.client;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @class: NodeDefinitionVersionDto
 * @description: NodeDefinitionVersionDto
 * @author: Ethan Liu
 * @create: 2021-09-08 19:49
 **/
@NoArgsConstructor
@Data
public class NodeDefinitionVersionDto {
    private String ownerRef;
    private String ref;
    private String creatorName;
    private String creatorRef;
    private String version;
    private String resultFile;
    private Set<Parameter> inputParameters;
    private Set<Parameter> outputParameters;
    private String spec;
}
