package dev.jianmu.embedded.worker.aggregate;


import dev.jianmu.embedded.worker.aggregate.spec.ContainerSpec;

/**
 * @author Ethan Liu
 * @class DockerTask
 * @description Docker任务封装
 * @create 2021-04-14 20:14
 */
public class EmbeddedWorkerTask {
    private String taskInstanceId;
    private String taskName;
    private String businessId;
    // 外部触发ID，流程实例唯一
    private String triggerId;
    // 任务定义唯一Key
    private String defKey;
    private String resultFile;
    // 容器规格定义
    private ContainerSpec spec;

    private EmbeddedWorkerTask() {
    }

    public String getTaskInstanceId() {
        return taskInstanceId;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getBusinessId() {
        return businessId;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public String getDefKey() {
        return defKey;
    }

    public String getResultFile() {
        return resultFile;
    }

    public ContainerSpec getSpec() {
        return spec;
    }

    public static final class Builder {
        private String taskInstanceId;
        private String taskName;
        private String businessId;
        private String triggerId;
        // 任务定义唯一Key
        private String defKey;
        private String resultFile;
        // 容器规格定义
        private ContainerSpec spec;

        private Builder() {
        }

        public static Builder aEmbeddedWorkerTask() {
            return new Builder();
        }

        public Builder taskInstanceId(String taskInstanceId) {
            this.taskInstanceId = taskInstanceId;
            return this;
        }

        public Builder taskName(String taskName) {
            this.taskName = taskName;
            return this;
        }

        public Builder businessId(String businessId) {
            this.businessId = businessId;
            return this;
        }

        public Builder triggerId(String triggerId) {
            this.triggerId = triggerId;
            return this;
        }

        public Builder defKey(String defKey) {
            this.defKey = defKey;
            return this;
        }

        public Builder resultFile(String resultFile) {
            this.resultFile = resultFile;
            return this;
        }

        public Builder spec(ContainerSpec spec) {
            this.spec = spec;
            return this;
        }

        public EmbeddedWorkerTask build() {
            EmbeddedWorkerTask embeddedWorkerTask = new EmbeddedWorkerTask();
            embeddedWorkerTask.spec = this.spec;
            embeddedWorkerTask.triggerId = this.triggerId;
            embeddedWorkerTask.businessId = this.businessId;
            embeddedWorkerTask.taskInstanceId = this.taskInstanceId;
            embeddedWorkerTask.taskName = this.taskName;
            embeddedWorkerTask.defKey = this.defKey;
            embeddedWorkerTask.resultFile = this.resultFile;
            return embeddedWorkerTask;
        }
    }
}
