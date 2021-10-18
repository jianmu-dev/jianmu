package dev.jianmu.workflow.aggregate.process;

import java.time.LocalDateTime;

/**
 * @program: workflow
 * @description: 异步任务执行实例
 * @author: Ethan Liu
 * @create: 2021-01-21 20:45
 **/
public class AsyncTaskInstance {
    // 显示名称
    private String name;
    // 描述
    private String description;
    // 运行状态
    private TaskStatus status = TaskStatus.INIT;
    // 任务定义唯一引用名称
    private String asyncTaskRef;
    // 任务定义类型
    private String asyncTaskType;
    // 开始时间
    private LocalDateTime startTime;
    // 结束时间
    private LocalDateTime endTime;

    void run() {
        this.status = TaskStatus.RUNNING;
        this.startTime = LocalDateTime.now();
    }

    void succeed() {
        this.status = TaskStatus.SUCCEEDED;
        this.endTime = LocalDateTime.now();
    }

    void fail() {
        this.status = TaskStatus.FAILED;
        this.endTime = LocalDateTime.now();
    }

    void skip() {
        this.status = TaskStatus.SKIPPED;
        this.startTime = LocalDateTime.now();
        this.endTime = LocalDateTime.now();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getAsyncTaskRef() {
        return asyncTaskRef;
    }

    public String getAsyncTaskType() {
        return asyncTaskType;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public static final class Builder {
        // 显示名称
        private String name;
        // 描述
        private String description;
        // 任务定义唯一引用名称
        private String asyncTaskRef;
        // 任务定义类型
        private String asyncTaskType;

        private Builder() {
        }

        public static Builder anAsyncTaskInstance() {
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

        public Builder asyncTaskRef(String asyncTaskRef) {
            this.asyncTaskRef = asyncTaskRef;
            return this;
        }

        public Builder asyncTaskType(String asyncTaskType) {
            this.asyncTaskType = asyncTaskType;
            return this;
        }

        public AsyncTaskInstance build() {
            AsyncTaskInstance asyncTaskInstance = new AsyncTaskInstance();
            asyncTaskInstance.description = this.description;
            asyncTaskInstance.name = this.name;
            asyncTaskInstance.asyncTaskRef = this.asyncTaskRef;
            asyncTaskInstance.asyncTaskType = this.asyncTaskType;
            return asyncTaskInstance;
        }
    }
}
