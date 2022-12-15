package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import javax.validation.constraints.NotBlank;

/**
 * @class ProjectCreatingDto
 * @description ProjectCreatingDto
 * @author Daihw
 * @create 2022/12/14 4:54 下午
 */
@Getter
public class ProjectCreatingDto {
    @Schema(description = "联合ID", required = true)
    @NotBlank(message = "联合ID不能为空")
    private String associationId;

    @Schema(description = "联合类型", required = true)
    @NotBlank(message = "联合类型不能为空")
    private String associationType;

    @Schema(description = "联合平台", required = true)
    @NotBlank(message = "联合平台不能为空")
    private String associationPlatform;

    @Schema(required = true)
    @NotBlank(message = "Dsl内容不能为空")
    private String dslText;
    @Schema(description = "项目组ID")
    private String projectGroupId;
    @Schema(description = "流水线分支")
    private String branch;
}
