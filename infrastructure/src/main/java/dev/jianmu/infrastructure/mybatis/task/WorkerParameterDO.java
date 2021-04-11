package dev.jianmu.infrastructure.mybatis.task;

/**
 * @class: WorkerParameterDO
 * @description: TODO
 * @author: Ethan Liu
 * @create: 2021-04-11 11:46
 **/
public class WorkerParameterDO {
    private String workerId;
    private String parameterKey;
    private String parameterValue;

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
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
