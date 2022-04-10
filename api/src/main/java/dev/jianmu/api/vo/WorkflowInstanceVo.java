package dev.jianmu.api.vo;

import dev.jianmu.workflow.aggregate.process.ProcessStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @class WorkflowInstanceVo
 * @description 流程实例VO
 * @author Ethan Liu
 * @create 2021-04-22 10:49
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "流程实例VO")
public class WorkflowInstanceVo {
    private String id;
    private String triggerId;
    private String triggerType;
    // 执行顺序号
    private int serialNo;
    private String name;
    private String workflowRef;
    private String workflowVersion;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime suspendedTime;
    private LocalDateTime endTime;
    private ProcessStatus status;
}
