package dev.jianmu.event.impl;

import lombok.*;

/**
 * @author Daihw
 * @class WorkflowInstanceCreatedEvent
 * @description 流程实例创建事件
 * @create 2022/9/19 2:14 下午
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class WorkflowInstanceCreatedEvent extends BaseEvent {
    private String id;
    private String triggerId;
    private String triggerType;
    private int serialNo;
    private String name;
    private String workflowRef;
    private String workflowVersion;
    private String description;
    private String status;
}
