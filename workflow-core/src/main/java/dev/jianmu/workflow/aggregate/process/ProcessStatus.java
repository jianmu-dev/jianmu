package dev.jianmu.workflow.aggregate.process;

/**
 * @author Ethan Liu
 * @program: workflow
 * @description 流程运行实例状态
 * @create 2021-01-21 19:53
 */
public enum ProcessStatus {
    RUNNING,
    SUSPENDED,
    TERMINATED,
    FINISHED
}
