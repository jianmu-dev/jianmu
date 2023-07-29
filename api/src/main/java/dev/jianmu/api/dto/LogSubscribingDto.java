package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @class LogSubscribingDto
 * @description LogSubscribingDto
 * @author Daihw
 * @create 2022/5/30 3:44 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "LogSubscribingDto")
public class LogSubscribingDto {
    @NotNull(message = "参数size不能为空")
    private Integer size;
}
