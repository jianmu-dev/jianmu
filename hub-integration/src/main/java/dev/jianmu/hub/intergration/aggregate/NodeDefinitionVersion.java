package dev.jianmu.hub.intergration.aggregate;

import java.util.HashSet;
import java.util.Set;

/**
 * @class: NodeDefinitionVersion
 * @description: 节点定义版本
 * @author: Ethan Liu
 * @create: 2021-09-03 14:58
 **/
public class NodeDefinitionVersion {
    public enum Type {
        DOCKER,
        SHELL
    }

    private String ref;
    private String version;
    private String resultFile;
    private Type type;

    private Set<NodeParameter> inputParameters = new HashSet<>();
    private Set<NodeParameter> outputParameters = new HashSet<>();

    private String spec;

    public String getRef() {
        return ref;
    }

    public String getVersion() {
        return version;
    }

    public String getResultFile() {
        return resultFile;
    }

    public Type getType() {
        return type;
    }

    public Set<NodeParameter> getInputParameters() {
        return inputParameters;
    }

    public Set<NodeParameter> getOutputParameters() {
        return outputParameters;
    }

    public String getSpec() {
        return spec;
    }


    public static final class Builder {
        private String ref;
        private String version;
        private String resultFile;
        private Type type;
        private Set<NodeParameter> inputParameters = new HashSet<>();
        private Set<NodeParameter> outputParameters = new HashSet<>();
        private String spec;

        private Builder() {
        }

        public static Builder aNodeDefinitionVersion() {
            return new Builder();
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

        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        public Builder inputParameters(Set<NodeParameter> inputParameters) {
            this.inputParameters = inputParameters;
            return this;
        }

        public Builder outputParameters(Set<NodeParameter> outputParameters) {
            this.outputParameters = outputParameters;
            return this;
        }

        public Builder spec(String spec) {
            this.spec = spec;
            return this;
        }

        public NodeDefinitionVersion build() {
            NodeDefinitionVersion nodeDefinitionVersion = new NodeDefinitionVersion();
            nodeDefinitionVersion.outputParameters = this.outputParameters;
            nodeDefinitionVersion.inputParameters = this.inputParameters;
            nodeDefinitionVersion.version = this.version;
            nodeDefinitionVersion.type = this.type;
            nodeDefinitionVersion.ref = this.ref;
            nodeDefinitionVersion.spec = this.spec;
            nodeDefinitionVersion.resultFile = this.resultFile;
            return nodeDefinitionVersion;
        }
    }
}
