package dev.jianmu.infrastructure.mybatis.task;

/**
 * @class: TaskWorkerParameterDO
 * @description: TODO
 * @author: Ethan Liu
 * @create: 2021-04-11 17:21
 **/
public class TaskWorkerParameterDO {
    private String taskDefinitionId;
    private String parameterKey;
    private String parameterValue;

    public String getTaskDefinitionId() {
        return taskDefinitionId;
    }

    public void setTaskDefinitionId(String taskDefinitionId) {
        this.taskDefinitionId = taskDefinitionId;
    }

    public String getParameterKey() {
        return parameterKey;
    }

    public void setParameterKey(String parameterKey) {
        this.parameterKey = parameterKey;
    }

    public String getParameterValue() {
        return parameterValue;
    }

    public void setParameterValue(String parameterValue) {
        this.parameterValue = parameterValue;
    }
}
