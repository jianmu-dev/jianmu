package dev.jianmu.api.dto;

import dev.jianmu.worker.aggregate.Worker;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author Daihw
 * @class WorkerJoiningDto
 * @description WorkerJoiningDto
 * @create 2022/5/19 11:33 上午
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Worker连接Dto")
public class WorkerJoiningDto {
    @NotNull(message = "参数type不能为空")
    @Schema(required = true, description = "type")
    private Worker.Type type;
    @NotBlank(message = "参数name不能为空")
    @Schema(required = true, description = "name")
    private String name;
}
