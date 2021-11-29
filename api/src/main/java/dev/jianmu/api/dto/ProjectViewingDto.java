package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @class ProjectViewingDto
 * @description 项目查看Dto
 * @author Daihw
 * @create 2021/11/24 4:07 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "项目查看Dto")
public class ProjectViewingDto extends PageDto {
    @NotBlank(message = "项目组ID不能为空")
    private String projectGroupId;
    private String workflowName;
}
