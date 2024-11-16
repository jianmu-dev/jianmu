package dev.jianmu.api.dto;

import dev.jianmu.worker.aggregate.Worker;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @Schema(required = true, description = "type")
    private Worker.Type type = Worker.Type.DOCKER;
    @Schema(required = true, description = "name")
    private String name;
    @Schema(required = true, description = "tag")
    private String tag;
    @Schema(description = "capacity")
    private Integer capacity;
    @Schema(description = "os")
    private String os;
    @Schema(description = "arch")
    private String arch;
}
