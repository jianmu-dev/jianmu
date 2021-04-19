package dev.jianmu.api.dto;

import dev.jianmu.task.aggregate.TaskParameter;
import dev.jianmu.task.aggregate.spec.ContainerSpec;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String ref;
    @Schema(required = true)
    private String version;
    private String description;
    @Schema(required = true)
    private String resultFile;
    private Set<TaskParameter> inputParameters;
    private Set<TaskParameter> outputParameters;
    @Schema(required = true)
    private ContainerSpec spec;
}
