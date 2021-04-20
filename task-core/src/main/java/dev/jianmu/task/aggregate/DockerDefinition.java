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
        // 唯一Key
        protected String key;
        protected String resultFile;
        // 输入输出参数列表
        protected Set<TaskParameter> inputParameters = new HashSet<>();
        protected Set<TaskParameter> outputParameters = new HashSet<>();
        private ContainerSpec spec;

        private Builder() {
        }

        public static Builder aDockerDefinition() {
            return new Builder();
        }

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder resultFile(String resultFile) {
            this.resultFile = resultFile;
            return this;
        }

        public Builder inputParameters(Set<TaskParameter> inputParameters) {
            this.inputParameters = inputParameters;
            return this;
        }

        public Builder outputParameters(Set<TaskParameter> outputParameters) {
            this.outputParameters = outputParameters;
            return this;
        }

        public Builder spec(ContainerSpec spec) {
            this.spec = spec;
            return this;
        }

        public DockerDefinition build() {
            DockerDefinition dockerDefinition = new DockerDefinition();
            dockerDefinition.spec = this.spec;
            dockerDefinition.resultFile = this.resultFile;
            dockerDefinition.outputParameters = this.outputParameters;
            dockerDefinition.key = this.key;
            dockerDefinition.inputParameters = this.inputParameters;
            return dockerDefinition;
        }
    }
}
