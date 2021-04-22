package dev.jianmu.api.dto;

import dev.jianmu.task.aggregate.TaskParameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * @class: TaskDefinitionVersionDto
 * @description: 任务定义版本DTO
 * @author: Ethan Liu
 * @create: 2021-04-19 15:59
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "任务定义版本DTO")
public class TaskDefinitionVersionDto {
    @Schema(required = true)
    @NotBlank(message = "ref不能为空")
    private String ref;
    @Schema(required = true)
    @NotBlank(message = "version不能为空")
    private String version;
    private String description;
    private String resultFile;
    @NotNull(message = "inputParameters不能为空")
    private Set<TaskParameter> inputParameters;
    @NotNull(message = "outputParameters不能为空")
    private Set<TaskParameter> outputParameters;
    @Schema(required = true)
    @NotNull(message = "spec不能为空")
    private ContainerSpecDto spec;
}
