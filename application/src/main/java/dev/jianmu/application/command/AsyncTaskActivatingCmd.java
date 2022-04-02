package dev.jianmu.application.command;

import lombok.Builder;
import lombok.Data;

/**
 * @author Ethan Liu
 * @class AsyncTaskActivatingCmd
 * @description 异步任务激活命令
 * @create 2022-01-02 11:39
 */
@Data
@Builder
public class AsyncTaskActivatingCmd {
    // 流程定义唯一引用名称
    private String workflowRef;
    // 流程定义版本
    private String workflowVersion;
    // 触发器ID
    private String triggerId;
    // 任务唯一引用名称
    private String asyncTaskRef;
    // 任务类型
    private String asyncTaskType;
    // 乐观锁版本
    private int version;
}
