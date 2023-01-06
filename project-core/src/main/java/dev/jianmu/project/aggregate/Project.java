package dev.jianmu.project.aggregate;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @class Project
 * @description 建木项目
 * @author Ethan Liu
 * @create 2021-04-23 10:55
*/
public class Project extends BaseAssociation {
    public enum DslSource {
        GIT,
        LOCAL
    }

    public enum DslType {
        WORKFLOW,
        PIPELINE
    }

    public enum TriggerType {
        EVENT_BRIDGE,
        WEBHOOK,
        CRON,
        MANUAL
    }

    // ID
    private String id;
    // DSL来源
    private DslSource dslSource;
    // DSL类型
    private DslType dslType;
    // 触发类型
    private TriggerType triggerType;
    // Git库Id
    private String gitRepoId;
    // 关联流程定义名称
    private String workflowName;
    // 关联流程定义描述
    private String workflowDescription;
    // 关联流程定义Ref
    private String workflowRef;
    // 关联流程定义版本
    private String workflowVersion;
    // 流程节点数量
    private int steps;
    // 项目状态
    private boolean enabled = true;
    // 状态是否可变
    private boolean mutable = true;
    // 可否并发执行
    private int concurrent;
    // 原始DSL文本
    private String dslText;
    // 创建者ID
    private String creatorId;
    // 创建时间
    private final LocalDateTime createdTime = LocalDateTime.now();
    // 最后修改者ID
    private String lastModifiedById;
    // 最后修改者
    private String lastModifiedBy;
    // 最后修改时间
    private LocalDateTime lastModifiedTime;

    public void switchEnabled(boolean enabled) {
        if (!this.mutable) {
            throw new RuntimeException("mutable为false时项目状态不可更改");
        }
        this.enabled = enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setMutable(boolean mutable) {
        this.mutable = mutable;
    }

    public void setConcurrent(int concurrent) {
        this.concurrent = concurrent;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public void setWorkflowDescription(String workflowDescription) {
        this.workflowDescription = workflowDescription;
    }

    public void setWorkflowVersion(String workflowVersion) {
        this.workflowVersion = workflowVersion;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public void setDslText(String dslText) {
        this.dslText = dslText;
    }

    public void setDslType(DslType dslType) {
        this.dslType = dslType;
    }

    public void setTriggerType(TriggerType triggerType) {
        this.triggerType = triggerType;
    }

    public void setLastModifiedById(String lastModifiedById) {
        this.lastModifiedById = lastModifiedById;
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

    public DslType getDslType() {
        return dslType;
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public String getGitRepoId() {
        return gitRepoId;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public String getWorkflowDescription() {
        return workflowDescription;
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

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isMutable() {
        return mutable;
    }

    public int getConcurrent() {
        return concurrent;
    }

    public String getDslText() {
        return dslText;
    }

    public String getLastModifiedById() {
        return lastModifiedById;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public LocalDateTime getLastModifiedTime() {
        return lastModifiedTime;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public static final class Builder {
        // DSL来源
        private DslSource dslSource;
        // DSL类型
        private DslType dslType;
        // 触发类型
        private TriggerType triggerType;
        // Git库Id
        private String gitRepoId;
        // 关联流程定义名称
        private String workflowName;
        // 关联流程定义描述
        private String workflowDescription;
        // 关联流程定义Ref
        private String workflowRef;
        // 关联流程定义版本
        private String workflowVersion;
        // 流程节点数量
        private int steps;
        // 项目状态
        private boolean enabled;
        // 状态是否可变
        private boolean mutable;
        // 可否并发执行
        private int concurrent;
        // 原始DSL文本
        private String dslText;
        // 创建者ID
        private String creatorId;
        // 最后修改者ID
        private String lastModifiedById;
        // 最后修改者
        private String lastModifiedBy;
        // 联合ID
        private String associationId;
        // 联合类型
        private String associationType;
        // 联合平台
        private String associationPlatform;

        private Builder() {
        }

        public static Builder aReference() {
            return new Builder();
        }

        public Builder dslSource(DslSource dslSource) {
            this.dslSource = dslSource;
            return this;
        }

        public Builder dslType(DslType dslType) {
            this.dslType = dslType;
            return this;
        }

        public Builder triggerType(TriggerType triggerType) {
            this.triggerType = triggerType;
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

        public Builder workflowDescription(String workflowDescription) {
            this.workflowDescription = workflowDescription;
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

        public Builder steps(int steps) {
            this.steps = steps;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder mutable(boolean mutable) {
            this.mutable = mutable;
            return this;
        }

        public Builder concurrent(int concurrent) {
            this.concurrent = concurrent;
            return this;
        }

        public Builder dslText(String dslText) {
            this.dslText = dslText;
            return this;
        }

        public Builder creatorId(String creatorId) {
            this.creatorId = creatorId;
            return this;
        }

        public Builder lastModifiedById(String lastModifiedById) {
            this.lastModifiedById = lastModifiedById;
            return this;
        }

        public Builder lastModifiedBy(String lastModifiedBy) {
            this.lastModifiedBy = lastModifiedBy;
            return this;
        }

        public Builder associationId(String associationId) {
            this.associationId = associationId;
            return this;
        }

        public Builder associationType(String associationType) {
            this.associationType = associationType;
            return this;
        }

        public Builder associationPlatform(String associationPlatform) {
            this.associationPlatform = associationPlatform;
            return this;
        }

        public Project build() {
            Project project = new Project();
            project.id = UUID.randomUUID().toString().replace("-", "");
            project.workflowVersion = this.workflowVersion;
            project.workflowName = this.workflowName;
            project.workflowDescription = this.workflowDescription;
            project.dslSource = this.dslSource;
            project.dslType = this.dslType;
            project.triggerType = this.triggerType;
            project.gitRepoId = this.gitRepoId;
            project.steps = this.steps;
            project.concurrent = this.concurrent;
            project.workflowRef = this.workflowRef;
            project.dslText = this.dslText;
            project.creatorId = this.creatorId;
            project.lastModifiedById = this.lastModifiedById;
            project.lastModifiedBy = this.lastModifiedBy;
            project.lastModifiedTime = LocalDateTime.now();
            project.updateAssociation(this.associationId, this.associationType, this.associationPlatform);
            return project;
        }
    }
}
