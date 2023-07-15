package dev.jianmu.api.dto;

import dev.jianmu.project.aggregate.Credential;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * @class GitRepoDto
 * @description GitRepoDto
 * @author Ethan Liu
 * @create 2021-05-13 19:18
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "GitRepoDto")
public class GitRepoDto {
    @NotBlank(message = "参数id不能为空", groups = AddGroup.class)
    private String id;
    @Schema(required = true)
    @NotBlank(message = "参数Uri不能为空")
    private String uri;
    @Schema(required = true)
    @NotNull(message = "参数credential不能为空")
    private Credential credential;
    @Schema(required = true)
    @NotBlank(message = "参数Branch不能为空")
    private String branch;
    @NotBlank(message = "参数dslPath不能为空", groups = AddGroup.class)
    private String dslPath;
    @NotBlank(message = "项目组ID不能为空")
    @Schema(description = "项目组ID")
    private String projectGroupId;
}
