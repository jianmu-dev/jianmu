package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * @class ProjectGroupAddingDto
 * @description 项目添加项目Dto
 * @author Daihw
 * @create 2021/11/26 11:11 上午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "项目添加项目Dto")
public class ProjectGroupAddingDto {
    @Schema(description = "项目组ID", required = true)
    @NotNull(message = "项目组ID不能为空")
    private String projectGroupId;

    @Schema(description = "项目ID集合", required = true)
    @NotNull(message = "项目ID集合不能为空")
    private List<String> projectIds;
}
