package dev.jianmu.infrastructure.client;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @class: DefinitionDto
 * @description: DefinitionDto
 * @author: Ethan Liu
 * @create: 2021-06-21 11:53
 **/
@NoArgsConstructor
@Data
public class DefinitionDto {

    private String createdAt;
    private String updatedAt;
    private Integer id;
    private String ref;
    private String version;
    private String resultFile;
    private String type;
    private List<InputParametersEntity> inputParameters;
    private List<OutputParametersEntity> outputParameters;
    private SpecEntity spec;
    private String dslSource;
    private MetaDataEntity metaData;

    @NoArgsConstructor
    @Data
    public static class SpecEntity {
        private List<String> cmd;
        private List<String> entrypoint;
        private String image;
    }

    @NoArgsConstructor
    @Data
    public static class MetaDataEntity {
        private String createdAt;
        private String updatedAt;
        private Integer id;
        private String name;
        private String description;
        private String docs;
        private String owner;
        private String source;
    }

    @NoArgsConstructor
    @Data
    public static class InputParametersEntity {
        private String name;
        private String ref;
        private String value;
        private String description;
    }

    @NoArgsConstructor
    @Data
    public static class OutputParametersEntity {
        private String name;
        private String ref;
        private String value;
    }
}
