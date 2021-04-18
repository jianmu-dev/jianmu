package dev.jianmu.version.aggregate;

/**
 * @class: TaskDefinitionVersion
 * @description: 任务定义版本
 * @author: Ethan Liu
 * @create: 2021-04-17 18:04
 **/
public class TaskDefinitionVersion {
    // 任务定义ID
    private String taskDefinitionId;
    // 版本名称
    private String name;
    // 任务定义Ref
    private String taskDefinitionRef;
    // 任务定义Key
    private String definitionKey;
    // 描述
    private String description;

    private TaskDefinitionVersion() {
    }

    public String getTaskDefinitionId() {
        return taskDefinitionId;
    }

    public String getName() {
        return name;
    }

    public String getTaskDefinitionRef() {
        return taskDefinitionRef;
    }

    public String getDefinitionKey() {
        return definitionKey;
    }

    public String getDescription() {
        return description;
    }

    public static final class Builder {
        private String taskDefinitionId;
        private String name;
        private String taskDefinitionRef;
        private String definitionKey;
        // 描述
        private String description;

        private Builder() {
        }

        public static Builder aTaskDefinitionVersion() {
            return new Builder();
        }

        public Builder taskDefinitionId(String taskDefinitionId) {
            this.taskDefinitionId = taskDefinitionId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder taskDefinitionRef(String taskDefinitionRef) {
            this.taskDefinitionRef = taskDefinitionRef;
            return this;
        }

        public Builder definitionKey(String definitionKey) {
            this.definitionKey = definitionKey;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public TaskDefinitionVersion build() {
            TaskDefinitionVersion taskDefinitionVersion = new TaskDefinitionVersion();
            taskDefinitionVersion.taskDefinitionId = this.taskDefinitionId;
            taskDefinitionVersion.taskDefinitionRef = this.taskDefinitionRef;
            taskDefinitionVersion.definitionKey = this.definitionKey;
            taskDefinitionVersion.name = this.name;
            taskDefinitionVersion.description = this.description;
            return taskDefinitionVersion;
        }
    }
}
