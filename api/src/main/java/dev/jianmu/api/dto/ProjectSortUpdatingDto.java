package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @class ProjectSortUpdatingDto
 * @description 修改项目排序Dto
 * @author Daihw
 * @create 2021/11/26 5:06 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "项目排序修改Dto")
public class ProjectSortUpdatingDto {
    @NotBlank(message = "原序号不能为空")
    @Schema(required = true, description = "原序号")
    private Integer originSort;

    @NotBlank(message = "目标序号不能为空")
    @Schema(required = true, description = "目标序号")
    private Integer targetSort;
}
