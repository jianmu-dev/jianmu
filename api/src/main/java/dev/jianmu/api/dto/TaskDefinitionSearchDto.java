package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @class: TaskDefinitionSearchDto
 * @description: TaskDefinitionSearchDto
 * @author: Ethan Liu
 * @create: 2021-04-27 09:03
 **/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "任务定义查询DTO")
public class TaskDefinitionSearchDto extends PageDto {
    private String name;
}
