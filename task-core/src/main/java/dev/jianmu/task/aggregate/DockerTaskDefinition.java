package dev.jianmu.task.aggregate;

import dev.jianmu.task.aggregate.spec.ContainerSpec;

import java.util.HashSet;
import java.util.Set;

/**
 * @class: DockerTaskDefinition
 * @description: Docker类型任务定义
 * @author: Ethan Liu
 * @create: 2021-04-15 10:26
 **/
public class DockerTaskDefinition extends BaseTaskDefinition {
    private ContainerSpec spec;

    private DockerTaskDefinition() {
    }

    public ContainerSpec getSpec() {
        return spec;
    }

    public static final class Builder {
        private ContainerSpec spec;
        // 显示名称
        private String name;
        // 描述
        private String description;
        // 唯一Key
        private String key;
        // 版本
        private String version;
        // 输入输出参数列表
        private Set<TaskParameter> parameters = new HashSet<>();

        private Builder() {
        }

        public static Builder aDockerTaskDefinition() {
            return new Builder();
        }

        public Builder spec(ContainerSpec spec) {
            this.spec = spec;
            return this;
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

        public Builder parameters(Set<TaskParameter> parameters) {
            this.parameters = parameters;
            return this;
        }

        public DockerTaskDefinition build() {
            DockerTaskDefinition dockerTaskDefinition = new DockerTaskDefinition();
            dockerTaskDefinition.parameters = this.parameters;
            dockerTaskDefinition.description = this.description;
            dockerTaskDefinition.key = this.key;
            dockerTaskDefinition.spec = this.spec;
            dockerTaskDefinition.version = this.version;
            dockerTaskDefinition.name = this.name;
            return dockerTaskDefinition;
        }
    }
}
