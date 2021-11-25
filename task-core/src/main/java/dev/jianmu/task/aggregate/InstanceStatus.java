package dev.jianmu.task.aggregate;

/**
 * @class TaskInstanceStatus
 * @description 任务实例状态枚举
 * @author Ethan Liu
 * @create 2021-03-25 16:19
*/
public enum InstanceStatus {
    WAITING,
    RUNNING,
    EXECUTION_SUCCEEDED,
    EXECUTION_FAILED,
    DISPATCH_FAILED
}
