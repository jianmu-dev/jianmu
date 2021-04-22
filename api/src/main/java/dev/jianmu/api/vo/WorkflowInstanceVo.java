package dev.jianmu.api.vo;

import dev.jianmu.workflow.aggregate.process.ProcessStatus;
import dev.jianmu.workflow.aggregate.process.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @class: WorkflowInstanceVo
 * @description: 流程实例VO
 * @author: Ethan Liu
 * @create: 2021-04-22 10:49
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "流程实例VO")
public class WorkflowInstanceVo {
    private String id;
    private String name;
    private String workflowVersion;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ProcessStatus status;
    private String latestTaskStatus;
}
