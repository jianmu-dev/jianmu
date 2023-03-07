package dev.jianmu.workflow.aggregate.definition;

import dev.jianmu.workflow.aggregate.process.FailureMode;

import java.util.*;

/**
 * @author Ethan Liu
 * @program: workflow
 * @description 流程组件父类
 * @create 2021-01-21 13:13
 */
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
    // 环路对列表
    protected List<LoopPair> loopPairs;
    // 类型
    protected String type;
    // 错误处理模式
    protected FailureMode failureMode = FailureMode.SUSPEND;
    // 节点元数据快照
    protected String metadata;
    // 参数列表
    protected Set<TaskParameter> taskParameters;
    // 缓存
    protected Set<TaskCache> taskCaches;

    protected BaseNode() {
    }

    @Override
    public void setFailureMode(FailureMode failureMode) {
        this.failureMode = failureMode;
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
    public void setLoopPairs(List<LoopPair> loopPairs) {
        this.loopPairs = loopPairs;
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
    public void addLoopPair(LoopPair loopPair) {
        if (loopPairs == null) {
            this.loopPairs = new ArrayList<>();
        }
        var checked = loopPairs.stream()
                .filter(l -> l.getSource().equals(loopPair.getSource()) && l.getTarget().equals(loopPair.getTarget()))
                .count();
        if (checked > 0) {
            return;
        }
        this.loopPairs.add(loopPair);
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
    public FailureMode getFailureMode() {
        return this.failureMode;
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
    public List<LoopPair> getLoopPairs() {
        if (loopPairs == null) {
            return new ArrayList<>();
        }
        return loopPairs;
    }

    @Override
    public Set<TaskParameter> getTaskParameters() {
        return taskParameters;
    }

    @Override
    public void setTaskParameters(Set<TaskParameter> taskParameters) {
        this.taskParameters = taskParameters;
    }

    @Override
    public Set<TaskCache> getTaskCaches() {
        return this.taskCaches;
    }

    @Override
    public void setTaskCaches(Set<TaskCache> taskCaches) {
        this.taskCaches = taskCaches;
    }
}
