package dev.jianmu.project.aggregate;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @class: Project
 * @description: 建木项目
 * @author: Ethan Liu
 * @create: 2021-04-23 10:55
 **/
public class Project {
    public enum DslSource {
        GIT,
        LOCAL
    }

    // ID
    private String id;
    // DSL来源
    private DslSource dslSource;
    // Git库Id
    private String gitRepoId;
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
    // 创建时间
    private final LocalDateTime createdTime = LocalDateTime.now();
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

    public DslSource getDslSource() {
        return dslSource;
    }

    public String getGitRepoId() {
        return gitRepoId;
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

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public static final class Builder {
        // DSL来源
        private DslSource dslSource;
        // Git库Id
        private String gitRepoId;
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

        public Builder dslSource(DslSource dslSource) {
            this.dslSource = dslSource;
            return this;
        }

        public Builder gitRepoId(String gitRepoId) {
            this.gitRepoId = gitRepoId;
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

        public Project build() {
            Project project = new Project();
            project.id = UUID.randomUUID().toString().replace("-", "");
            project.workflowVersion = UUID.randomUUID().toString().replace("-", "");
            project.workflowName = this.workflowName;
            project.dslSource = this.dslSource;
            project.gitRepoId = this.gitRepoId;
            project.steps = this.steps;
            project.workflowRef = this.workflowRef;
            project.dslText = this.dslText;
            project.lastModifiedBy = this.lastModifiedBy;
            project.lastModifiedTime = LocalDateTime.now();
            return project;
        }
    }
}
