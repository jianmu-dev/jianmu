package dev.jianmu.dsl.aggregate;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @class: Reference
 * @description: DSL流程定义关联
 * @author: Ethan Liu
 * @create: 2021-04-23 10:55
 **/
public class DslReference {
    // ID
    private String id;
    // DSL文件地址
    private String dslUrl;
    // 关联流程定义名称
    private String workflowName;
    // 关联流程定义Ref
    private String workflowRef;
    // 关联流程定义版本
    private String workflowVersion;
    // 流程节点数量
    private int steps;
    // 原始DSL文本
    private String dslText;
    // 最后修改者
    private String lastModifiedBy;
    // 最后修改时间
    private LocalDateTime lastModifiedTime;

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public void setWorkflowVersion() {
        this.workflowVersion = UUID.randomUUID().toString().replace("-", "");
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public void setDslText(String dslText) {
        this.dslText = dslText;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public void setLastModifiedTime() {
        this.lastModifiedTime = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getDslUrl() {
        return dslUrl;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public String getWorkflowRef() {
        return workflowRef;
    }

    public String getWorkflowVersion() {
        return workflowVersion;
    }

    public int getSteps() {
        return steps;
    }

    public String getDslText() {
        return dslText;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public LocalDateTime getLastModifiedTime() {
        return lastModifiedTime;
    }

    public static final class Builder {
        // DSL文件地址
        private String dslUrl;
        // 关联流程定义名称
        private String workflowName;
        // 关联流程定义Ref
        private String workflowRef;
        // 流程节点数量
        private int steps;
        // 原始DSL文本
        private String dslText;
        // 最后修改者
        private String lastModifiedBy;

        private Builder() {
        }

        public static Builder aReference() {
            return new Builder();
        }

        public Builder dslUrl(String dslUrl) {
            this.dslUrl = dslUrl;
            return this;
        }

        public Builder workflowName(String workflowName) {
            this.workflowName = workflowName;
            return this;
        }

        public Builder workflowRef(String workflowRef) {
            this.workflowRef = workflowRef;
            return this;
        }

        public Builder steps(int steps) {
            this.steps = steps;
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

        public DslReference build() {
            DslReference dslReference = new DslReference();
            dslReference.id = UUID.randomUUID().toString().replace("-", "");
            dslReference.workflowVersion = UUID.randomUUID().toString().replace("-", "");
            dslReference.workflowName = this.workflowName;
            dslReference.dslUrl = this.dslUrl;
            dslReference.steps = this.steps;
            dslReference.workflowRef = this.workflowRef;
            dslReference.dslText = this.dslText;
            dslReference.lastModifiedBy = this.lastModifiedBy;
            dslReference.lastModifiedTime = LocalDateTime.now();
            return dslReference;
        }
    }
}
