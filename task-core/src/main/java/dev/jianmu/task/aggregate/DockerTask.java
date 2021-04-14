package dev.jianmu.task.aggregate;

import java.util.List;
import java.util.Map;

/**
 * @class: DockerTask
 * @description: Docker任务封装
 * @author: Ethan Liu
 * @create: 2021-04-14 20:14
 **/
public class DockerTask {
    private String taskInstanceId;
    private String businessId;
    private String triggerId;
    // 任务定义唯一Key
    private String defKey;
    // 任务定义版本
    private String defVersion;
    // 容器规格定义
    private ContainerSpec spec;

    private DockerTask() {
    }

    public String getTaskInstanceId() {
        return taskInstanceId;
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

    public String getDefVersion() {
        return defVersion;
    }

    public ContainerSpec getSpec() {
        return spec;
    }

    public static final class Builder {
        private String taskInstanceId;
        private String businessId;
        private String triggerId;
        // 任务定义唯一Key
        private String defKey;
        // 任务定义版本
        private String defVersion;
        // 容器规格定义
        private ContainerSpec spec;

        private Builder() {
        }

        public static Builder aDockerTask() {
            return new Builder();
        }

        public Builder taskInstanceId(String taskInstanceId) {
            this.taskInstanceId = taskInstanceId;
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

        public Builder defVersion(String defVersion) {
            this.defVersion = defVersion;
            return this;
        }

        public Builder spec(ContainerSpec spec) {
            this.spec = spec;
            return this;
        }

        public DockerTask build() {
            DockerTask dockerTask = new DockerTask();
            dockerTask.spec = this.spec;
            dockerTask.triggerId = this.triggerId;
            dockerTask.defVersion = this.defVersion;
            dockerTask.businessId = this.businessId;
            dockerTask.taskInstanceId = this.taskInstanceId;
            dockerTask.defKey = this.defKey;
            return dockerTask;
        }
    }
}
