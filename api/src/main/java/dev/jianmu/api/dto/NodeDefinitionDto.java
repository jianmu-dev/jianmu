package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @class NodeDefinitionDto
 * @description NodeDefinitionDto
 * @author Ethan Liu
 * @create 2021-10-01 11:27
*/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "NodeDefinitionDto")
public class NodeDefinitionDto {
    @NotBlank
    private String name;
    private String description;
    @NotBlank
    private String dsl;
}
