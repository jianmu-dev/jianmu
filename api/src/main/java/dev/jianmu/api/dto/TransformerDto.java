package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @class: TransformerDto
 * @description: TransformerDto
 * @author: Ethan Liu
 * @create: 2021-09-26 14:26
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "TransformerDto")
public class TransformerDto {
    public enum Type {
        BODY,
        HEADER,
        QUERY
    }

    @NotNull
    private Type type;
    @NotBlank
    private String variableName;
    @NotBlank
    private String variableType;
    @NotBlank
    private String expression;
}
