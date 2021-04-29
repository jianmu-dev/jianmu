package dev.jianmu.task.aggregate;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    public Set<TaskParameter> getInputParametersWith(List<InputParameter> inputParameters) {
        return outputParameters.stream()
                .peek(taskParameter -> inputParameters.stream()
                        .filter(inputParameter -> inputParameter.getRef().equals(taskParameter.getRef()))
                        .findFirst()
                        .ifPresent(inputParameter -> taskParameter.setParameterId(inputParameter.getParameterId()))
                )
                .collect(Collectors.toSet());
    }

    @Override
    public Set<TaskParameter> matchedOutputParameters(Map<String, Object> parameterMap) {
        return outputParameters.stream()
                .filter(taskParameter -> parameterMap.get(taskParameter.getRef()) != null)
                .collect(Collectors.toSet());
    }

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
