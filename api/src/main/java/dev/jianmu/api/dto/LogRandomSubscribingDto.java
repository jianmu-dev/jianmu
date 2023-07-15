package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * @author Daihw
 * @class LogRandomSubscribingDto
 * @description LogRandomSubscribingDto
 * @create 2022/5/30 4:25 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "LogRandomSubscribingDto")
public class LogRandomSubscribingDto {
    @NotNull(message = "参数size不能为空")
    private Integer line;
    @NotNull(message = "参数size不能为空")
    private Integer size;
}
