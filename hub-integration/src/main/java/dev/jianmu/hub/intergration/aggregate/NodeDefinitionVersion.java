package dev.jianmu.hub.intergration.aggregate;

import dev.jianmu.hub.intergration.aggregate.spec.ContainerSpec;

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

    private ContainerSpec spec;

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

    public ContainerSpec getSpec() {
        return spec;
    }
}
