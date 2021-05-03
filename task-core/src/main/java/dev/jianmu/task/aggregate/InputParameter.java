package dev.jianmu.task.aggregate;

/**
 * @class: InputParameter
 * @description: 任务定义输入覆盖参数
 * @author: Ethan Liu
 * @create: 2021-04-28 16:08
 **/
public class InputParameter {
    // 任务定义Key, 表示任务定义类型
    private String defKey;
    // 流程定义上下文中的AsyncTask唯一标识
    private String asyncTaskRef;
    // 流程定义Ref
    private String workflowRef;
    // 流程定义版本
    private String workflowVersion;
    // 项目ID
    private String projectId;
    // 参数唯一引用名称
    private String ref;
    // 参数引用Id
    private String parameterId;

    public String getDefKey() {
        return defKey;
    }

    public String getAsyncTaskRef() {
        return asyncTaskRef;
    }

    public String getWorkflowRef() {
        return workflowRef;
    }

    public String getWorkflowVersion() {
        return workflowVersion;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getRef() {
        return ref;
    }

    public String getParameterId() {
        return parameterId;
    }

    public static final class Builder {
        // 任务定义Key, 表示任务定义类型
        private String defKey;
        // 流程定义上下文中的AsyncTask唯一标识
        private String asyncTaskRef;
        // 流程定义Ref
        private String workflowRef;
        // 流程定义版本
        private String workflowVersion;
        // 项目ID
        private String projectId;
        // 参数唯一引用名称
        private String ref;
        // 参数引用Id
        private String parameterId;

        private Builder() {
        }

        public static Builder anInputParameter() {
            return new Builder();
        }

        public Builder defKey(String defKey) {
            this.defKey = defKey;
            return this;
        }

        public Builder asyncTaskRef(String asyncTaskRef) {
            this.asyncTaskRef = asyncTaskRef;
            return this;
        }

        public Builder workflowRef(String workflowRef) {
            this.workflowRef = workflowRef;
            return this;
        }

        public Builder workflowVersion(String workflowVersion) {
            this.workflowVersion = workflowVersion;
            return this;
        }

        public Builder projectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder ref(String ref) {
            this.ref = ref;
            return this;
        }

        public Builder parameterId(String parameterId) {
            this.parameterId = parameterId;
            return this;
        }

        public InputParameter build() {
            InputParameter inputParameter = new InputParameter();
            inputParameter.defKey = this.defKey;
            inputParameter.parameterId = this.parameterId;
            inputParameter.ref = this.ref;
            inputParameter.projectId = this.projectId;
            inputParameter.asyncTaskRef = this.asyncTaskRef;
            inputParameter.workflowRef = this.workflowRef;
            inputParameter.workflowVersion = this.workflowVersion;
            return inputParameter;
        }
    }
}
