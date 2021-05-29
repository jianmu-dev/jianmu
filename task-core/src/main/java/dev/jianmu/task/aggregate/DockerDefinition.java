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
        this.type = Worker.Type.DOCKER;
    }

    public ContainerSpec getSpec() {
        return spec;
    }

    public static final class Builder {
        // 唯一Key
        protected String key;
        protected String ref;
        protected String version;
        protected String resultFile;
        protected Worker.Type type;
        // 输入输出参数列表
        protected Set<TaskParameter> inputParameters = new HashSet<>();
        protected Set<TaskParameter> outputParameters = new HashSet<>();
        protected MetaData metaData;
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

        public Builder ref(String ref) {
            this.ref = ref;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder resultFile(String resultFile) {
            this.resultFile = resultFile;
            return this;
        }

        public Builder type(Worker.Type type) {
            this.type = type;
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

        public Builder metaData(MetaData metaData) {
            this.metaData = metaData;
            return this;
        }

        public Builder spec(ContainerSpec spec) {
            this.spec = spec;
            return this;
        }

        public DockerDefinition build() {
            DockerDefinition dockerDefinition = new DockerDefinition();
            dockerDefinition.ref = this.ref;
            dockerDefinition.resultFile = this.resultFile;
            dockerDefinition.metaData = this.metaData;
            dockerDefinition.outputParameters = this.outputParameters;
            dockerDefinition.type = this.type;
            dockerDefinition.spec = this.spec;
            dockerDefinition.inputParameters = this.inputParameters;
            dockerDefinition.key = this.key;
            dockerDefinition.version = this.version;
            return dockerDefinition;
        }
    }
}
