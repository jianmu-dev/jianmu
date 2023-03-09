package dev.jianmu.api.vo;

import dev.jianmu.workflow.aggregate.definition.TaskCache;
import dev.jianmu.workflow.aggregate.process.FailureMode;
import dev.jianmu.workflow.aggregate.process.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Ethan Liu
 * @class AsyncTaskInstanceVo
 * @description 异步任务实例Vo
 * @create 2022-04-13 15:17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "异步任务实例Vo")
public class AsyncTaskInstanceVo {
    // ID
    private String id;
    // 触发器ID
    private String triggerId;
    // 流程定义唯一引用名称
    private String workflowRef;
    // 流程定义版本
    private String workflowVersion;
    // 流程定义版本
    private String workflowInstanceId;
    // 显示名称
    private String name;
    // 描述
    private String description;
    // 运行状态
    private TaskStatus status;
    // 错误处理模式
    private FailureMode failureMode = FailureMode.SUSPEND;
    // 任务定义唯一引用名称
    private String asyncTaskRef;
    // 任务定义类型
    private String asyncTaskType;
    // 完成次数计数，从0开始
    private int serialNo;
    // 开始时间
    private LocalDateTime startTime;
    // 结束时间
    private LocalDateTime endTime;
    // 任务缓存
    private List<TaskCache> taskCaches;
}
