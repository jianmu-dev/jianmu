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
 * @class: TaskDefinitionDto
 * @description: 任务定义DTO
 * @author: Ethan Liu
 * @create: 2021-04-18 20:59
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "任务定义DTO")
public class TaskDefinitionDto {
    private String name;
    private String ref;
    private String version;
    private String description;
    private Set<TaskParameter> taskParameters;
    private ContainerSpec spec;
}
