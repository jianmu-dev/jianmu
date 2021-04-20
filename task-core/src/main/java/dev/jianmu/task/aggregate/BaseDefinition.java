package dev.jianmu.task.aggregate;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @class: TaskDefinition
 * @description: 任务定义
 * @author: Ethan Liu
 * @create: 2021-03-25 15:44
 **/
public class BaseDefinition implements Definition {
    // 唯一Key
    protected String key;

    protected String resultFile;

    protected Worker.Type type;

    // 输入输出参数列表
    protected Set<TaskParameter> inputParameters = new HashSet<>();

    protected Set<TaskParameter> outputParameters = new HashSet<>();

    @Override
    public Set<TaskParameter> getInputParameters() {
        return inputParameters;
    }

    @Override
    public Set<TaskParameter> getOutputParameters() {
        return outputParameters;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getResultFile() {
        return this.resultFile;
    }

    @Override
    public Worker.Type getType() {
        return type;
    }
}
