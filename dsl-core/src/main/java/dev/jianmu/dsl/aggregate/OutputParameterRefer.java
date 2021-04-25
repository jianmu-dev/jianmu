package dev.jianmu.dsl.aggregate;

/**
 * @class: OutputParameterRef
 * @description: 输出参数引用关系
 * @author: Ethan Liu
 * @create: 2021-04-25 11:28
 **/
public class OutputParameterRefer {
    private String projectId;
    private String workflowVersion;
    private String outputNodeName;
    private String outputNodeType;
    private String outputParameterRef;
    private String outputParameterId;
    private String inputNodeName;
    private String inputNodeType;
    private String inputParameterRef;
    private String inputParameterId;
    private String contextId;

    @Override
    public String toString() {
        return "OutputParameterRefer{" +
                "projectId='" + projectId + '\'' +
                ", workflowVersion='" + workflowVersion + '\'' +
                ", outputNodeName='" + outputNodeName + '\'' +
                ", outputNodeType='" + outputNodeType + '\'' +
                ", outputParameterRef='" + outputParameterRef + '\'' +
                ", outputParameterId='" + outputParameterId + '\'' +
                ", inputNodeName='" + inputNodeName + '\'' +
                ", inputNodeType='" + inputNodeType + '\'' +
                ", inputParameterRef='" + inputParameterRef + '\'' +
                ", inputParameterId='" + inputParameterId + '\'' +
                ", contextId='" + contextId + '\'' +
                '}';
    }

    public String getProjectId() {
        return projectId;
    }

    public String getWorkflowVersion() {
        return workflowVersion;
    }

    public String getOutputNodeName() {
        return outputNodeName;
    }

    public String getOutputNodeType() {
        return outputNodeType;
    }

    public String getOutputParameterRef() {
        return outputParameterRef;
    }

    public String getOutputParameterId() {
        return outputParameterId;
    }

    public String getInputNodeName() {
        return inputNodeName;
    }

    public String getInputNodeType() {
        return inputNodeType;
    }

    public String getInputParameterRef() {
        return inputParameterRef;
    }

    public String getInputParameterId() {
        return inputParameterId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public void setWorkflowVersion(String workflowVersion) {
        this.workflowVersion = workflowVersion;
    }

    public void setOutputNodeName(String outputNodeName) {
        this.outputNodeName = outputNodeName;
    }

    public void setOutputNodeType(String outputNodeType) {
        this.outputNodeType = outputNodeType;
    }

    public void setOutputParameterRef(String outputParameterRef) {
        this.outputParameterRef = outputParameterRef;
    }

    public void setOutputParameterId(String outputParameterId) {
        this.outputParameterId = outputParameterId;
    }

    public void setInputNodeName(String inputNodeName) {
        this.inputNodeName = inputNodeName;
    }

    public void setInputNodeType(String inputNodeType) {
        this.inputNodeType = inputNodeType;
    }

    public void setInputParameterRef(String inputParameterRef) {
        this.inputParameterRef = inputParameterRef;
    }

    public void setInputParameterId(String inputParameterId) {
        this.inputParameterId = inputParameterId;
    }

    public String getContextId() {
        return contextId;
    }

    // contextId规则,使用project id + WorkflowVersion + NodeName
    public void setContextId() {
        this.contextId = projectId + workflowVersion + inputNodeName;
    }

    public static final class Builder {
        private String projectId;
        private String workflowVersion;
        private String outputNodeName;
        private String outputNodeType;
        private String outputParameterRef;
        private String outputParameterId;
        private String inputNodeName;
        private String inputNodeType;
        private String inputParameterRef;
        private String inputParameterId;

        private Builder() {
        }

        public static Builder anOutputParameterRef() {
            return new Builder();
        }

        public Builder projectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder workflowVersion(String workflowVersion) {
            this.workflowVersion = workflowVersion;
            return this;
        }

        public Builder outputNodeName(String outputNodeName) {
            this.outputNodeName = outputNodeName;
            return this;
        }

        public Builder outputNodeType(String outputNodeType) {
            this.outputNodeType = outputNodeType;
            return this;
        }

        public Builder outputParameterRef(String outputParameterRef) {
            this.outputParameterRef = outputParameterRef;
            return this;
        }

        public Builder outputParameterId(String outputParameterId) {
            this.outputParameterId = outputParameterId;
            return this;
        }

        public Builder inputNodeName(String inputNodeName) {
            this.inputNodeName = inputNodeName;
            return this;
        }

        public Builder inputNodeType(String inputNodeType) {
            this.inputNodeType = inputNodeType;
            return this;
        }

        public Builder inputParameterRef(String inputParameterRef) {
            this.inputParameterRef = inputParameterRef;
            return this;
        }

        public Builder inputParameterId(String inputParameterId) {
            this.inputParameterId = inputParameterId;
            return this;
        }

        public OutputParameterRefer build() {
            OutputParameterRefer outputParameterRefer = new OutputParameterRefer();
            outputParameterRefer.setProjectId(projectId);
            outputParameterRefer.setWorkflowVersion(workflowVersion);
            outputParameterRefer.setOutputNodeName(outputNodeName);
            outputParameterRefer.setOutputNodeType(outputNodeType);
            outputParameterRefer.setOutputParameterRef(outputParameterRef);
            outputParameterRefer.setOutputParameterId(outputParameterId);
            outputParameterRefer.setInputNodeName(inputNodeName);
            outputParameterRefer.setInputNodeType(inputNodeType);
            outputParameterRefer.setInputParameterRef(inputParameterRef);
            outputParameterRefer.setInputParameterId(inputParameterId);
            return outputParameterRefer;
        }
    }
}
