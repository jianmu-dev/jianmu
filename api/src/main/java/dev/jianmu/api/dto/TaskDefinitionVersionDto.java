package dev.jianmu.api.dto;

import dev.jianmu.task.aggregate.TaskParameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
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
    @NotBlank
    private String ref;
    @Schema(required = true)
    @NotBlank
    private String version;
    private String description;
    private String resultFile;
    private Set<TaskParameter> inputParameters;
    private Set<TaskParameter> outputParameters;
    @Schema(required = true)
    private ContainerSpecDto spec;
}
