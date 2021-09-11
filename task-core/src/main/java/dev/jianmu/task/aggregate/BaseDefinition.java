package dev.jianmu.task.aggregate;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @class: TaskDefinition
 * @description: 任务定义
 * @author: Ethan Liu
 * @create: 2021-03-25 15:44
 **/
public class BaseDefinition implements Definition {
    protected String ref;

    protected String version;

    protected String resultFile;

    protected Worker.Type type;

    // 输入输出参数列表
    protected Set<TaskParameter> inputParameters = new HashSet<>();

    protected Set<TaskParameter> outputParameters = new HashSet<>();

    protected MetaData metaData;

    @Override
    public Optional<TaskParameter> getInputParameterBy(String ref) {
        return inputParameters.stream()
                .filter(taskParameter -> taskParameter.getRef().equals(ref))
                .findFirst();
    }

    @Override
    public Set<TaskParameter> matchedOutputParameters(Map<String, Object> parameterMap) {
        return outputParameters.stream()
                .filter(taskParameter -> parameterMap.get(taskParameter.getRef()) != null)
                .collect(Collectors.toSet());
    }

    @Override
    public MetaData getMetaData() {
        return metaData;
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
    public String getRef() {
        return ref;
    }

    @Override
    public String getVersion() {
        return version;
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
