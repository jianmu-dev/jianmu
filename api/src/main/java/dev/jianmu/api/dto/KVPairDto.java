package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @class KVPairDto
 * @description 键值对Dto
 * @author Ethan Liu
 * @create 2021-04-20 13:03
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "键值对Dto")
public class KVPairDto {
    @Schema(required = true)
    @NotBlank(message = "参数Key不能为空")
    private String key;
    @Schema(required = true)
    @NotBlank(message = "参数Value不能为空")
    private String value;
}
