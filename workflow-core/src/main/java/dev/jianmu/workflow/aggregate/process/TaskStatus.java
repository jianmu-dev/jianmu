package dev.jianmu.workflow.aggregate.process;

/**
 * @author Ethan Liu
 * @program: workflow
 * @description 任务运行实例状态
 * @create 2021-01-21 19:53
 */
public enum TaskStatus {
    INIT,
    WAITING,
    RUNNING,
    SUSPENDED,
    SKIPPED,
    FAILED,
    IGNORED,
    SUCCEEDED
}
