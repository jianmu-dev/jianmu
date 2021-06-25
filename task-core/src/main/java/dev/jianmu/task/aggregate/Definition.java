package dev.jianmu.task.aggregate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @class: TaskDefinition
 * @description: 任务定义接口
 * @author: Ethan Liu
 * @create: 2021-04-15 10:45
 **/
public interface Definition {
    String getRef();

    String getVersion();

    String getResultFile();

    Worker.Type getType();

    Set<TaskParameter> getInputParameters();

    Optional<TaskParameter> getInputParameterBy(String ref);

    Set<TaskParameter> getInputParametersWith(List<InputParameter> inputParameters, Map<String, InstanceParameter> instanceOutputParameters);

    Set<TaskParameter> getOutputParameters();

    Set<TaskParameter> matchedOutputParameters(Map<String, Object> parameterMap);

    MetaData getMetaData();
}
