package dev.jianmu.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @class: ErrorMessage
 * @description: Rest API异常信息封装DTO
 * @author: Ethan Liu
 * @create: 2021-04-06 20:47
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "API 异常信息包装类")
public class ErrorMessage {
    @Schema(description = "HTTP 错误码")
    private int statusCode;
    @Schema(description = "时间戳")
    private LocalDateTime timestamp;
    @Schema(description = "错误信息")
    private String message;
    @Schema(description = "描述")
    private String description;
}
