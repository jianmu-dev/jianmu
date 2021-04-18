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
public class DockerDefinition extends BaseDefinition {
    private ContainerSpec spec;

    private DockerDefinition() {
    }

    public ContainerSpec getSpec() {
        return spec;
    }

    public static final class Builder {
        private ContainerSpec spec;
        // 唯一Key
        private String key;

        private Builder() {
        }

        public static Builder aDockerTaskDefinition() {
            return new Builder();
        }

        public Builder spec(ContainerSpec spec) {
            this.spec = spec;
            return this;
        }

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public DockerDefinition build() {
            DockerDefinition dockerTaskDefinition = new DockerDefinition();
            dockerTaskDefinition.key = this.key;
            dockerTaskDefinition.spec = this.spec;
            return dockerTaskDefinition;
        }
    }
}
