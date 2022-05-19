package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * @class WorkerJoiningDto
 * @description WorkerJoiningDto
 * @author Daihw
 * @create 2022/5/19 11:33 上午
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Worker连接Dto")
public class WorkerJoiningDto {
    @NotBlank(message = "参数secret不能为空")
    @Schema(required = true, description = "secret")
    private String secret;
}
