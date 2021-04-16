package dev.jianmu.task.aggregate;

import java.util.HashSet;
import java.util.Set;

/**
 * @class: TaskDefinition
 * @description: 任务定义
 * @author: Ethan Liu
 * @create: 2021-03-25 15:44
 **/
public class BaseTaskDefinition implements TaskDefinition {
    // 显示名称
    protected String name;
    // 描述
    protected String description;

    // 唯一Key
    protected String key;
    // 版本
    protected String version;

    protected Worker.Type type;

    // 输入输出参数列表
    protected Set<TaskParameter> parameters = new HashSet<>();

    @Override
    public Set<TaskParameter> getParameters() {
        return Set.copyOf(parameters);
    }

    @Override
    public void setParameters(Set<TaskParameter> parameters) {
        this.parameters = Set.copyOf(parameters);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Worker.Type getType() {
        return type;
    }
}
