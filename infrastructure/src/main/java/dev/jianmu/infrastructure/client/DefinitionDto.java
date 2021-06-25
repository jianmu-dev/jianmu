package dev.jianmu.infrastructure.client;

import dev.jianmu.task.aggregate.TaskParameter;
import dev.jianmu.task.aggregate.spec.ContainerSpec;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @class: DefinitionDto
 * @description: DefinitionDto
 * @author: Ethan Liu
 * @create: 2021-06-21 11:53
 **/
@NoArgsConstructor
@Data
public class DefinitionDto {
    private Integer id;
    private String ref;
    private String version;
    private String resultFile;
    private String type;
    private Set<TaskParameter> inputParameters;
    private Set<TaskParameter> outputParameters;
    private ContainerSpec spec;
    private String dslSource;
    private MetaDataEntity metaData;

    @NoArgsConstructor
    @Data
    public static class MetaDataEntity {
        private Integer id;
        private String name;
        private String description;
        private String icon;
        private String group;
        private String tags;
        private String docs;
        private String owner;
        private String source;
    }
}
