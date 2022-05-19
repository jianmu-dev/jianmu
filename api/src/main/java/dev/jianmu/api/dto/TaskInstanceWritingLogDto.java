package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author Daihw
 * @class TaskInstanceWritingLogDto
 * @description TaskInstanceWritingLogDto
 * @create 2022/5/19 11:14 上午
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "写入任务日志Dto")
public class TaskInstanceWritingLogDto {
    @NotNull(message = "参数number不能为空")
    @Schema(required = true, description = "行号")
    private Long number;

    @NotBlank(message = "参数content不能为空")
    @Schema(required = true, description = "内容")
    private String content;

    @NotNull(message = "参数timestamp不能为空")
    @Schema(required = true, description = "时间戳")
    private Long timestamp;
}
