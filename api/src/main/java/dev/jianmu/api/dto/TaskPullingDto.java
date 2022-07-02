package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @class TaskPullingDto
 * @description TaskPullingDto
 * @author Daihw
 * @create 2022/6/27 11:41 上午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "LoginDto")
public class TaskPullingDto {
    private String triggerId;
}
