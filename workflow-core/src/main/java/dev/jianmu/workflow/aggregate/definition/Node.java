package dev.jianmu.workflow.aggregate.definition;

import java.util.Set;

/**
 * @program: workflow
 * @description: 流程节点接口定义
 * @author: Ethan Liu
 * @create: 2021-01-21 14:13
 **/
public interface Node {
    // 返回节点显示名称
    String getName();

    // 返回节点唯一引用名称
    String getRef();

    // 返回节点描述
    String getDescription();

    // 返回节点类型
    String getType();

    // 返回节点元数据快照
    String getMetadata();

    // 返回上游节点列表
    Set<String> getSources();

    // 返回下游节点列表
    Set<String> getTargets();

    // 设置上游节点列表
    void setSources(Set<String> sources);

    // 设置下游节点列表
    void setTargets(Set<String> targets);

    // 增加上游节点
    void addSource(String source);

    // 增加下游节点
    void addTarget(String target);

    // 获取参数列表
    Set<TaskParameter> getTaskParameters();

    // 设置参数列表
    void setTaskParameters(Set<TaskParameter> taskParameters);
}
