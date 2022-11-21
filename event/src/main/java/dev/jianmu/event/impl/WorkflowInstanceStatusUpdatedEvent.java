package dev.jianmu.event.impl;

import lombok.*;

/**
 * @author Daihw
 * @class WorkflowInstanceStatusUpdatedEvent
 * @description 流程实例状态更新事件
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
    private String startTime;
    // 挂起时间
    private String suspendedTime;
    // 结束时间
    private String endTime;
}
