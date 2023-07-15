package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

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
    @NotBlank(message = "密钥名称不能为空")
    @Pattern(regexp = "^\\w+$", message = "密钥名称只能输入由数字、26个英文字母或者下划线组成的字符串")
    private String key;
    @Schema(required = true)
    @NotBlank(message = "密钥值不能为空")
    private String value;
}
