package dev.jianmu.workflow.aggregate.process;

/**
 * @program: workflow
 * @description 任务运行实例状态
 * @author Ethan Liu
 * @create 2021-01-21 19:53
*/
public enum TaskStatus {
    INIT,
    RUNNING,
    SKIPPED,
    FAILED,
    SUCCEEDED
}
