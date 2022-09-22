package dev.jianmu.event.impl;

import lombok.*;

import java.time.LocalDateTime;

/**
 * @class WorkflowInstanceStatusUpdatedEvent
 * @description 流程实例状态更新事件
 * @author Daihw
 * @create 2022/9/15 1:49 下午
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class WorkflowInstanceStatusUpdatedEvent extends BaseEvent {
    private String id;
    private String status;
    private String workflowRef;
    private String workflowVersion;
    // 开始时间
    private LocalDateTime startTime;
    // 挂起时间
    private LocalDateTime suspendedTime;
    // 结束时间
    private LocalDateTime endTime;
}
