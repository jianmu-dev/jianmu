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

    public static final class Builder {
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

        private Builder() {
        }

        public static Builder aParameterRefer() {
            return new Builder();
        }

        public Builder workflowRef(String workflowRef) {
            this.workflowRef = workflowRef;
            return this;
        }

        public Builder workflowVersion(String workflowVersion) {
            this.workflowVersion = workflowVersion;
            return this;
        }

        public Builder sourceTaskRef(String sourceTaskRef) {
            this.sourceTaskRef = sourceTaskRef;
            return this;
        }

        public Builder sourceParameterRef(String sourceParameterRef) {
            this.sourceParameterRef = sourceParameterRef;
            return this;
        }

        public Builder targetTaskRef(String targetTaskRef) {
            this.targetTaskRef = targetTaskRef;
            return this;
        }

        public Builder targetParameterRef(String targetParameterRef) {
            this.targetParameterRef = targetParameterRef;
            return this;
        }

        public ParameterRefer build() {
            ParameterRefer parameterRefer = new ParameterRefer();
            parameterRefer.workflowRef = this.workflowRef;
            parameterRefer.sourceTaskRef = this.sourceTaskRef;
            parameterRefer.targetTaskRef = this.targetTaskRef;
            parameterRefer.sourceParameterRef = this.sourceParameterRef;
            parameterRefer.workflowVersion = this.workflowVersion;
            parameterRefer.targetParameterRef = this.targetParameterRef;
            return parameterRefer;
        }
    }
}
