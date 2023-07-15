package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * @class NamespaceDto
 * @description 命名空间Dto
 * @author Ethan Liu
 * @create 2021-04-20 12:51
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "命名空间Dto")
public class NamespaceDto {
    @Schema(required = true)
    @NotBlank(message = "命名空间名称不能为空")
    @Pattern(regexp = "^\\w+$", message = "命名空间名称只能输入由数字、26个英文字母或者下划线组成的字符串")
    private String name;
    private String description;
}
