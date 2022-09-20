package dev.jianmu.event.impl;

import lombok.*;

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
}
