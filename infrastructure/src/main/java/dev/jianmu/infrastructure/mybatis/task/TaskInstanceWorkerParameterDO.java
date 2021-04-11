package dev.jianmu.infrastructure.mybatis.task;

/**
 * @class: TaskInstanceWorkerParameterDO
 * @description: TODO
 * @author: Ethan Liu
 * @create: 2021-04-11 19:19
 **/
public class TaskInstanceWorkerParameterDO {
    private String taskInstanceId;
    private String parameterKey;
    private String parameterValue;

    public String getTaskInstanceId() {
        return taskInstanceId;
    }

    public void setTaskInstanceId(String taskInstanceId) {
        this.taskInstanceId = taskInstanceId;
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
