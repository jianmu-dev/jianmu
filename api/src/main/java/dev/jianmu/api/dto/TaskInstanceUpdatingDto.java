package dev.jianmu.api.dto;

import dev.jianmu.task.aggregate.InstanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

/**
 * @class TaskInstanceUpdatingDto
 * @description TaskInstanceUpdatingDto
 * @author Daihw
 * @create 2022/5/19 10:59 上午
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "更新任务Dto")
public class TaskInstanceUpdatingDto {
    public enum Status{
        RUNNING,
        SUCCEED,
        FAILED
    }

    @NotNull(message = "参数status不能为空")
    @Schema(required = true, description = "任务状态")
    private Status status;

    @Schema(description = "返回文件")
    private String resultFile;

    @Schema(description = "退出码")
    private Integer exitCode;

    @Schema(description = "错误信息")
    private String errorMsg;
}
