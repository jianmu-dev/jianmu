package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @class ProjectGroupDto
 * @description 项目组Dto
 * @author Daihw
 * @create 2021/11/25 3:29 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "项目组Dto")
public class ProjectGroupDto {
    @Schema(required = true)
    @NotBlank(message = "名称不能为空")
    private String name;
    private String description;
    @NotNull(message = "是否展示不能为空")
    private Boolean isShow;
}
