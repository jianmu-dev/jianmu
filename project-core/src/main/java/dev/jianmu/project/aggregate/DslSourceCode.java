package dev.jianmu.project.aggregate;

import java.time.LocalDateTime;

/**
 * @class: DslSourceCode
 * @description: DslSourceCode
 * @author: Ethan Liu
 * @create: 2021-04-23 22:50
 **/
public class DslSourceCode {
    // 项目ID
    private String projectId;
    // 关联流程定义Ref
    private String workflowRef;
    // 关联流程定义版本
    private String workflowVersion;
    // 原始DSL文本
    private String dslText;
    // 创建时间
    private final LocalDateTime createdTime = LocalDateTime.now();
    // 最后修改者
    private String lastModifiedBy;
    // 最后修改时间
    private final LocalDateTime lastModifiedTime = LocalDateTime.now();

    public String getProjectId() {
        return projectId;
    }

    public String getWorkflowRef() {
        return workflowRef;
    }

    public String getWorkflowVersion() {
        return workflowVersion;
    }

    public String getDslText() {
        return dslText;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public LocalDateTime getLastModifiedTime() {
        return lastModifiedTime;
    }

    public static final class Builder {
        // 项目ID
        private String projectId;
        // 关联流程定义Ref
        private String workflowRef;
        // 关联流程定义版本
        private String workflowVersion;
        // 原始DSL文本
        private String dslText;
        // 最后修改者
        private String lastModifiedBy;

        private Builder() {
        }

        public static Builder aDslSourceCode() {
            return new Builder();
        }

        public Builder projectId(String projectId) {
            this.projectId = projectId;
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

        public Builder dslText(String dslText) {
            this.dslText = dslText;
            return this;
        }

        public Builder lastModifiedBy(String lastModifiedBy) {
            this.lastModifiedBy = lastModifiedBy;
            return this;
        }

        public DslSourceCode build() {
            DslSourceCode dslSourceCode = new DslSourceCode();
            dslSourceCode.workflowRef = this.workflowRef;
            dslSourceCode.projectId = this.projectId;
            dslSourceCode.dslText = this.dslText;
            dslSourceCode.lastModifiedBy = this.lastModifiedBy;
            dslSourceCode.workflowVersion = this.workflowVersion;
            return dslSourceCode;
        }
    }
}
