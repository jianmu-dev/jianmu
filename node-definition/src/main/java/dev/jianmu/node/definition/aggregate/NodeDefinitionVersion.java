package dev.jianmu.node.definition.aggregate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ethan Liu
 * @class NodeDefinitionVersion
 * @description 节点定义版本
 * @create 2021-09-03 14:58
 */
public class NodeDefinitionVersion {

    private String id;
    private String ownerRef;
    private String ref;
    private String description;
    private String creatorName;
    private String creatorRef;
    private String version;
    private String resultFile;

    private List<NodeParameter> inputParameters = new ArrayList<>();
    private List<NodeParameter> outputParameters = new ArrayList<>();

    private String spec;

    public String getId() {
        return id;
    }

    public String getOwnerRef() {
        return ownerRef;
    }

    public String getRef() {
        return ref;
    }

    public String getDescription() {
        return description;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public String getCreatorRef() {
        return creatorRef;
    }

    public String getVersion() {
        return version;
    }

    public String getResultFile() {
        return resultFile;
    }

    public List<NodeParameter> getInputParameters() {
        return inputParameters;
    }

    public List<NodeParameter> getOutputParameters() {
        return outputParameters;
    }

    public String getSpec() {
        return spec;
    }

    public static final class Builder {
        private String id;
        private String ownerRef;
        private String ref;
        private String description;
        private String creatorName;
        private String creatorRef;
        private String version;
        private String resultFile;
        private List<NodeParameter> inputParameters = new ArrayList<>();
        private List<NodeParameter> outputParameters = new ArrayList<>();
        private String spec;

        private Builder() {
        }

        public static Builder aNodeDefinitionVersion() {
            return new Builder();
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder ownerRef(String ownerRef) {
            this.ownerRef = ownerRef;
            return this;
        }

        public Builder ref(String ref) {
            this.ref = ref;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder creatorName(String creatorName) {
            this.creatorName = creatorName;
            return this;
        }

        public Builder creatorRef(String creatorRef) {
            this.creatorRef = creatorRef;
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

        public Builder inputParameters(List<NodeParameter> inputParameters) {
            this.inputParameters = inputParameters;
            return this;
        }

        public Builder outputParameters(List<NodeParameter> outputParameters) {
            this.outputParameters = outputParameters;
            return this;
        }

        public Builder spec(String spec) {
            this.spec = spec;
            return this;
        }

        public NodeDefinitionVersion build() {
            NodeDefinitionVersion nodeDefinitionVersion = new NodeDefinitionVersion();
            nodeDefinitionVersion.id = this.id;
            nodeDefinitionVersion.outputParameters = this.outputParameters;
            nodeDefinitionVersion.creatorRef = this.creatorRef;
            nodeDefinitionVersion.inputParameters = this.inputParameters;
            nodeDefinitionVersion.version = this.version;
            nodeDefinitionVersion.ownerRef = this.ownerRef;
            nodeDefinitionVersion.ref = this.ref;
            nodeDefinitionVersion.creatorName = this.creatorName;
            nodeDefinitionVersion.spec = this.spec;
            nodeDefinitionVersion.resultFile = this.resultFile;
            nodeDefinitionVersion.description = this.description;
            return nodeDefinitionVersion;
        }
    }
}
