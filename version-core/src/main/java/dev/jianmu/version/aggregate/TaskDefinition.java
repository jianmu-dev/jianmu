package dev.jianmu.version.aggregate;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @class: TaskDefinition
 * @description: 任务定义
 * @author: Ethan Liu
 * @create: 2021-04-17 18:01
 **/
public class TaskDefinition {
    // ID
    // TODO 暂时使用UUID的值
    private String id = UUID.randomUUID().toString().replace("-", "");
    // 显示名称
    private String name;
    // Ref
    private String ref;
    // 创建时间
    private final LocalDateTime createdTime = LocalDateTime.now();
    // 最后修改时间
    private LocalDateTime lastModifiedTime = LocalDateTime.now();

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRef() {
        return ref;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public LocalDateTime getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public static final class Builder {
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
            taskDefinition.name = this.name;
            taskDefinition.ref = this.ref;
            taskDefinition.lastModifiedTime = LocalDateTime.now();
            return taskDefinition;
        }
    }
}
