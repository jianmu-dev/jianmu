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

    protected Worker.Type type;

    // 输入输出参数列表
    protected Set<TaskParameter> parameters = new HashSet<>();

    @Override
    public Set<TaskParameter> getParameters() {
        return Set.copyOf(parameters);
    }

    public void setParameters(Set<TaskParameter> parameters, Map<String, String> parameterMap) {
        parameters.forEach(taskParameter -> {
            var parameterId = parameterMap.get(taskParameter.getRef());
            taskParameter.setParameterId(parameterId);
        });
        this.parameters = Set.copyOf(parameters);
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Worker.Type getType() {
        return type;
    }
}
