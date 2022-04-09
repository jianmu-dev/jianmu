package dev.jianmu.application.command;

import lombok.Builder;
import lombok.Data;

/**
 * @author Ethan Liu
 * @class TaskActivatingCmd
 * @description TaskActivatingCmd
 * @create 2022-04-07 17:37
 */
@Data
@Builder
public class TaskActivatingCmd {
    // 流程定义唯一引用名称
    private String workflowRef;
    // 流程定义版本
    private String workflowVersion;
    // 流程实例ID
    private String workflowInstanceId;
    // 触发器ID
    private String triggerId;
    // 异步任务实例ID
    private String asyncTaskInstanceId;
    // 节点唯一引用名称
    private String nodeRef;
    // 节点类型
    private String nodeType;
}
