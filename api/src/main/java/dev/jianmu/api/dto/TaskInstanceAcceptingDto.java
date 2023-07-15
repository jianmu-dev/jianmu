package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

/**
 * @class TaskInstanceAcceptingDto
 * @description TaskInstanceAcceptingDto
 * @author Daihw
 * @create 2022/5/19 3:56 下午
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "任务实例确定Dto")
public class TaskInstanceAcceptingDto {
    @NotNull(message = "参数version不能为空")
    @Schema(required = true, description = "任务版本")
    private Integer version;
}
