package dev.jianmu.api.vo;

import dev.jianmu.task.aggregate.InstanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @class: TaskInstanceVo
 * @description: 任务实例Vo
 * @author: Ethan Liu
 * @create: 2021-04-22 13:51
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "任务实例Vo")
public class TaskInstanceVo {
    private String instanceId;
    private String nodeName;
    private String taskName;
    private String taskVersion;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private InstanceStatus status;
}
