package dev.jianmu.task.aggregate;

/**
 * @class: TaskDefinition
 * @description: 任务定义
 * @author: Ethan Liu
 * @create: 2021-03-25 15:44
 **/
public class TaskDefinition extends AggregateRoot {
    // 显示名称
    private String name;
    // 描述
    private String description;

    // 唯一Key
    private String key;
    // 版本
    private String version;

    // 执行环境类型
    private EnvType envType;

    private TaskDefinition() {
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getKey() {
        return key;
    }

    public String getVersion() {
        return version;
    }

    public EnvType getEnvType() {
        return envType;
    }

    public static final class Builder {
        // 显示名称
        private String name;
        // 描述
        private String description;
        // 唯一Key
        private String key;
        // 版本
        private String version;
        // 执行环境类型
        private EnvType envType;

        private Builder() {
        }

        public static Builder aDefinition() {
            return new Builder();
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder envType(EnvType envType) {
            this.envType = envType;
            return this;
        }

        public TaskDefinition build() {
            TaskDefinition taskDefinition = new TaskDefinition();
            taskDefinition.envType = this.envType;
            taskDefinition.description = this.description;
            taskDefinition.version = this.version;
            taskDefinition.name = this.name;
            taskDefinition.key = this.key;
            return taskDefinition;
        }
    }
}
