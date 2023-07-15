package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * @author Daihw
 * @class ProjectGroupSortUpdateDto
 * @description 项目组排序修改Dto
 * @create 2021/11/25 4:12 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "项目组排序修改Dto")
public class ProjectGroupSortUpdatingDto {
    @NotBlank(message = "原项目组ID不能为空")
    @Schema(required = true, description = "原项目组ID")
    private String originGroupId;

    @NotBlank(message = "目标项目组ID不能为空")
    @Schema(required = true, description = "目标项目组ID")
    private String targetGroupId;
}
