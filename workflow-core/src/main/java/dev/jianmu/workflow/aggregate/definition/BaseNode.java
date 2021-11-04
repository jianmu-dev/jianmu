package dev.jianmu.workflow.aggregate.definition;

import java.util.HashSet;
import java.util.Set;

/**
 * @program: workflow
 * @description: 流程组件父类
 * @author: Ethan Liu
 * @create: 2021-01-21 13:13
 **/
public abstract class BaseNode implements Node {
    // 显示名称
    protected String name;
    // 唯一引用名称
    protected String ref;
    // 描述
    protected String description;
    // 上游节点列表
    protected Set<String> sources = new HashSet<>();
    // 下游节点列表
    protected Set<String> targets = new HashSet<>();
    // 类型
    protected String type;
    // 节点元数据快照
    protected String metadata;
    // 参数列表
    protected Set<TaskParameter> taskParameters;

    protected BaseNode() {
    }

    @Override
    public void setSources(Set<String> sources) {
        this.sources = Set.copyOf(sources);
    }

    @Override
    public void setTargets(Set<String> targets) {
        this.targets = Set.copyOf(targets);
    }

    @Override
    public void addSource(String source) {
        this.sources.add(source);
    }

    @Override
    public void addTarget(String target) {
        this.targets.add(target);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getRef() {
        return ref;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public String getMetadata() {
        return this.metadata;
    }

    @Override
    public Set<String> getSources() {
        return Set.copyOf(this.sources);
    }

    @Override
    public Set<String> getTargets() {
        return Set.copyOf(this.targets);
    }

    @Override
    public Set<TaskParameter> getTaskParameters() {
        return taskParameters;
    }

    @Override
    public void setTaskParameters(Set<TaskParameter> taskParameters) {
        this.taskParameters = taskParameters;
    }
}
