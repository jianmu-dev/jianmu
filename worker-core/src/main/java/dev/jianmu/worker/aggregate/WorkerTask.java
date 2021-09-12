package dev.jianmu.worker.aggregate;

import java.util.Map;

/**
 * @class: WorkerTask
 * @description: WorkerTask
 * @author: Ethan Liu
 * @create: 2021-09-10 22:17
 **/
public class WorkerTask {
    private String workerId;
    private String taskInstanceId;
    private String businessId;
    // 外部触发ID，流程实例唯一
    private String triggerId;
    // 任务定义唯一Key
    private String defKey;
    private String resultFile;
    // 容器规格定义
    private String spec;
    private Map<String, String> parameterMap;

    public String getWorkerId() {
        return workerId;
    }

    public String getTaskInstanceId() {
        return taskInstanceId;
    }

    public String getBusinessId() {
        return businessId;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public String getDefKey() {
        return defKey;
    }

    public String getResultFile() {
        return resultFile;
    }

    public String getSpec() {
        return spec;
    }

    public Map<String, String> getParameterMap() {
        return parameterMap;
    }

    public static final class Builder {
        private String workerId;
        private String taskInstanceId;
        private String businessId;
        // 外部触发ID，流程实例唯一
        private String triggerId;
        // 任务定义唯一Key
        private String defKey;
        private String resultFile;
        // 容器规格定义
        private String spec;
        private Map<String, String> parameterMap;

        private Builder() {
        }

        public static Builder aWorkerTask() {
            return new Builder();
        }

        public Builder workerId(String workerId) {
            this.workerId = workerId;
            return this;
        }

        public Builder taskInstanceId(String taskInstanceId) {
            this.taskInstanceId = taskInstanceId;
            return this;
        }

        public Builder businessId(String businessId) {
            this.businessId = businessId;
            return this;
        }

        public Builder triggerId(String triggerId) {
            this.triggerId = triggerId;
            return this;
        }

        public Builder defKey(String defKey) {
            this.defKey = defKey;
            return this;
        }

        public Builder resultFile(String resultFile) {
            this.resultFile = resultFile;
            return this;
        }

        public Builder spec(String spec) {
            this.spec = spec;
            return this;
        }

        public Builder parameterMap(Map<String, String> parameterMap) {
            this.parameterMap = parameterMap;
            return this;
        }

        public WorkerTask build() {
            WorkerTask workerTask = new WorkerTask();
            workerTask.businessId = this.businessId;
            workerTask.resultFile = this.resultFile;
            workerTask.triggerId = this.triggerId;
            workerTask.spec = this.spec;
            workerTask.taskInstanceId = this.taskInstanceId;
            workerTask.workerId = this.workerId;
            workerTask.defKey = this.defKey;
            workerTask.parameterMap = this.parameterMap;
            return workerTask;
        }
    }
}
