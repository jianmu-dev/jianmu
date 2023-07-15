package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * @author Daihw
 * @class ProjectSortUpdatingDto
 * @description 修改项目排序Dto
 * @create 2021/11/26 5:06 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "项目排序修改Dto")
public class ProjectSortUpdatingDto {
    @NotBlank(message = "原项目ID不能为空")
    @Schema(required = true, description = "原项目ID")
    private String originProjectId;

    @NotBlank(message = "目标项目ID不能为空")
    @Schema(required = true, description = "目标项目ID")
    private String targetProjectId;
}
