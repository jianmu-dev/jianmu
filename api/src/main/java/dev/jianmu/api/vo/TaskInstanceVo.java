package dev.jianmu.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author Ethan Liu
 * @class TaskInstanceVo
 * @description 任务实例Vo
 * @create 2021-04-22 13:51
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "任务实例Vo")
public class TaskInstanceVo {
    public enum Status {
        INIT,
        WAITING,
        RUNNING,
        SUSPENDED,
        SKIPPED,
        FAILED,
        IGNORED,
        SUCCEEDED,
    }

    private String instanceId;
    private String businessId;
    private String nodeName;
    private String defKey;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Status status;
}
