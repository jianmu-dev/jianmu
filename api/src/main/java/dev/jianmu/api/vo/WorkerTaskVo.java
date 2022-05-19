package dev.jianmu.api.vo;

import dev.jianmu.infrastructure.docker.ContainerSpec;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Daihw
 * @class WorkerTaskVo
 * @description WorkerTaskVo
 * @create 2022/5/19 10:35 上午
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Worker任务Vo")
public class WorkerTaskVo {
    @Schema(description = "类型")
    private Type type;

    @Schema(description = "任务实例ID")
    private String taskInstanceId;

    @Schema(description = "拉取策略")
    private String pullStrategy;

    @Schema(description = "容器规格定义")
    private ContainerSpec containerSpec;

    @Schema(description = "返回文件地址")
    private String resultFile;

    @Schema(description = "volume")
    private VolumeVo volume;

    @Schema(description = "任务版本")
    private Integer version;

    public enum Type {
        TASK,
        VOLUME
    }
}
