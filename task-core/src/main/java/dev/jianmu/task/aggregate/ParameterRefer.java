package dev.jianmu.task.aggregate;

/**
 * @class: ParameterRefer
 * @description: 参数关联
 * @author: Ethan Liu
 * @create: 2021-04-29 15:03
 **/
public class ParameterRefer {
    // 流程定义Ref
    private String workflowRef;
    // 流程定义版本
    private String workflowVersion;
    // 源任务(输出的任务)Ref
    private String sourceTaskRef;
    // 源参数Ref
    private String sourceParameterRef;
    // 目标任务(引用的任务)Ref
    private String targetTaskRef;
    // 目标参数Ref
    private String targetParameterRef;

    public String getWorkflowRef() {
        return workflowRef;
    }

    public String getWorkflowVersion() {
        return workflowVersion;
    }

    public String getSourceTaskRef() {
        return sourceTaskRef;
    }

    public String getSourceParameterRef() {
        return sourceParameterRef;
    }

    public String getTargetTaskRef() {
        return targetTaskRef;
    }

    public String getTargetParameterRef() {
        return targetParameterRef;
    }
}
