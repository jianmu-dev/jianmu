package dev.jianmu.event.impl;

import lombok.*;

/**
 * @author Daihw
 * @class AsyncTaskInstanceStatusUpdatedEvent
 * @description 异步任务实例状态更新事件
 * @create 2022/9/15 1:52 下午
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AsyncTaskInstanceStatusUpdatedEvent extends BaseEvent {
    private String workflowRef;
    private String workflowVersion;
    private String workflowInstanceId;
    private String id;
    private String status;
    private String asyncTaskRef;
    private String asyncTaskType;
    // 开始时间
    private String startTime;
    // 结束时间
    private String endTime;
}
