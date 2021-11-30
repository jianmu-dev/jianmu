package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @class ProjectGroupSortUpdateDto
 * @description 项目排序修改Dto
 * @author Daihw
 * @create 2021/11/25 4:12 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "项目组排序修改Dto")
public class ProjectGroupSortUpdatingDto {
    @NotNull(message = "原序号不能为空")
    @Min(value = 0, message = "原序号最小为0")
    @Schema(required = true, description = "原序号")
    private Integer originSort;

    @NotNull(message = "目标序号不能为空")
    @Min(value = 0, message = "目标序号最小为0")
    @Schema(required = true, description = "目标序号")
    private Integer targetSort;
}
