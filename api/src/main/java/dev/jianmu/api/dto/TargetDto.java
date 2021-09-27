package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.Set;

/**
 * @class: TargetDto
 * @description: TargetDto
 * @author: Ethan Liu
 * @create: 2021-09-26 14:27
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "TargetDto")
public class TargetDto {
    @NotBlank
    private String ref;
    @NotBlank
    private String name;
    private Set<TransformerDto> transformers;
}
