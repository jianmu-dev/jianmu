package dev.jianmu.trigger.aggregate;

import java.util.Set;
import java.util.UUID;

/**
 * @class: Trigger
 * @description: 触发器
 * @author: Ethan Liu
 * @create: 2021-04-06 13:19
 **/
public class Trigger {
    public enum Type {
        CRON,
        EVERY,
        EVENT
    }

    public enum Category {
        WORKFLOW,
        TASK
    }

    // ID
    private String id;
    // 关联流程定义ID
    private String workflowId;
    // 关联任务定义ID
    private String taskDefinitionId;
    // 工作区
    private String workspace;
    // 触发器类型
    private Type type;
    // 触发器类别
    private Category category;
    // 参数列表
    private Set<TriggerParameter> triggerParameters;

    private Trigger() {
    }

    public void setTriggerParameters(Set<TriggerParameter> triggerParameters) {
        this.triggerParameters = triggerParameters;
    }

    public String getId() {
        return id;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public String getTaskDefinitionId() {
        return taskDefinitionId;
    }

    public String getWorkspace() {
        return workspace;
    }

    public Type getType() {
        return type;
    }

    public Category getCategory() {
        return category;
    }

    public Set<TriggerParameter> getParameters() {
        return triggerParameters;
    }

    public static final class Builder {
        // ID
        // TODO 暂时使用UUID的值
        private String id = UUID.randomUUID().toString().replace("-", "");
        // 关联流程定义ID
        private String workflowId;
        // 关联任务定义ID
        private String taskDefinitionId;
        // 工作区
        private String workspace;
        // 触发器类型
        private Type type;
        // 触发器类别
        private Category category;

        private Builder() {
        }

        public static Builder aTrigger() {
            return new Builder();
        }

        public Builder workflowId(String workflowId) {
            this.workflowId = workflowId;
            return this;
        }

        public Builder taskDefinitionId(String taskDefinitionId) {
            this.taskDefinitionId = taskDefinitionId;
            return this;
        }

        public Builder workspace(String workspace) {
            this.workspace = workspace;
            return this;
        }

        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        public Builder category(Category category) {
            this.category = category;
            return this;
        }

        public Trigger build() {
            Trigger trigger = new Trigger();
            trigger.workflowId = this.workflowId;
            trigger.id = this.id;
            trigger.category = this.category;
            trigger.taskDefinitionId = this.taskDefinitionId;
            trigger.workspace = this.workspace;
            trigger.type = this.type;
            return trigger;
        }
    }
}
