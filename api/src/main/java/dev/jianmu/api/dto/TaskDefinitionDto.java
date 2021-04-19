package dev.jianmu.api.dto;

import dev.jianmu.task.aggregate.Worker;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @class: TaskDefinitionDto
 * @description: 任务定义DTO
 * @author: Ethan Liu
 * @create: 2021-04-18 20:59
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "任务定义DTO")
public class TaskDefinitionDto extends TaskDefinitionVersionDto {
    @Schema(required = true)
    private String name;
    @Schema(required = true)
    private Worker.Type type;
}
