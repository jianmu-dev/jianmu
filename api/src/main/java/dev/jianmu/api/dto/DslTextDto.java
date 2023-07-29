package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @class DslTextDto
 * @description DslTextDto
 * @author Ethan Liu
 * @create 2021-04-26 17:13
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Dsl文本Dto")
public class DslTextDto {
    @Schema(required = true)
    @NotBlank(message = "Dsl内容不能为空")
    private String dslText;
    @NotBlank(message = "项目组ID不能为空")
    @Schema(description = "项目组ID")
    private String projectGroupId;
}
