package dev.jianmu.version.aggregate;

import java.util.UUID;

/**
 * @class: TaskDefinition
 * @description: 任务定义
 * @author: Ethan Liu
 * @create: 2021-04-17 18:01
 **/
public class TaskDefinition {
    // ID
    private String id;
    // 显示名称
    private String name;
    // Ref
    private String ref;

    private TaskDefinition() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRef() {
        return ref;
    }

    public static final class Builder {
        // ID
        // TODO 暂时使用UUID的值
        private String id = UUID.randomUUID().toString().replace("-", "");
        // 显示名称
        private String name;
        // Ref
        private String ref;

        private Builder() {
        }

        public static Builder aTaskDefinition() {
            return new Builder();
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder ref(String ref) {
            this.ref = ref;
            return this;
        }

        public TaskDefinition build() {
            TaskDefinition taskDefinition = new TaskDefinition();
            taskDefinition.id = this.id;
            taskDefinition.name = this.name;
            taskDefinition.ref = this.ref;
            return taskDefinition;
        }
    }
}
