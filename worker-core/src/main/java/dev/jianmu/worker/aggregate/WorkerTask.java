package dev.jianmu.worker.aggregate;

import java.util.List;
import java.util.Map;

/**
 * @class WorkerTask
 * @description WorkerTask
 * @author Ethan Liu
 * @create 2021-09-10 22:17
*/
public class WorkerTask {
    private String workerId;
    private Worker.Type type;
    private String taskInstanceId;
    private String taskName;
    private String businessId;
    // 外部触发ID，流程实例唯一
    private String triggerId;
    // 任务定义唯一Key
    private String defKey;
    private String resultFile;
    // 容器规格定义
    private String spec;
    private Map<String, String> parameterMap;
    // 是否为恢复执行
    private boolean resumed;
    // 是否为Shell类型任务
    private boolean shellTask;
    // 镜像名称
    private String image;
    // 命令列表
    private List<String> script;

    public String getWorkerId() {
        return workerId;
    }

    public Worker.Type getType() {
        return type;
    }

    public String getTaskInstanceId() {
        return taskInstanceId;
    }

    public String getTaskName() {
        return taskName;
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

    public boolean isResumed() {
        return resumed;
    }

    public boolean isShellTask() {
        return shellTask;
    }

    public String getImage() {
        return image;
    }

    public List<String> getScript() {
        return script;
    }

    public static final class Builder {
        private String workerId;
        private Worker.Type type;
        private String taskInstanceId;
        private String taskName;
        private String businessId;
        // 外部触发ID，流程实例唯一
        private String triggerId;
        // 任务定义唯一Key
        private String defKey;
        private String resultFile;
        // 容器规格定义
        private String spec;
        private Map<String, String> parameterMap;
        // 是否为恢复执行
        private boolean resumed;
        // 是否为Shell类型任务
        private boolean shellTask;
        // 镜像名称
        private String image;
        // 命令列表
        private List<String> script;

        private Builder() {
        }

        public static Builder aWorkerTask() {
            return new Builder();
        }

        public Builder workerId(String workerId) {
            this.workerId = workerId;
            return this;
        }

        public Builder type(Worker.Type type) {
            this.type = type;
            return this;
        }

        public Builder taskInstanceId(String taskInstanceId) {
            this.taskInstanceId = taskInstanceId;
            return this;
        }

        public Builder taskName(String taskName) {
            this.taskName = taskName;
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

        public Builder resumed(boolean resumed) {
            this.resumed = resumed;
            return this;
        }

        public Builder shellTask(boolean shellTask) {
            this.shellTask = shellTask;
            return this;
        }

        public Builder image(String image) {
            this.image = image;
            return this;
        }

        public Builder script(List<String> script) {
            this.script = script;
            return this;
        }

        public WorkerTask build() {
            WorkerTask workerTask = new WorkerTask();
            workerTask.triggerId = this.triggerId;
            workerTask.defKey = this.defKey;
            workerTask.type = this.type;
            workerTask.businessId = this.businessId;
            workerTask.parameterMap = this.parameterMap;
            workerTask.spec = this.spec;
            workerTask.taskInstanceId = this.taskInstanceId;
            workerTask.taskName = this.taskName;
            workerTask.resultFile = this.resultFile;
            workerTask.workerId = this.workerId;
            workerTask.resumed = this.resumed;
            workerTask.shellTask = this.shellTask;
            workerTask.image = this.image;
            workerTask.script = this.script;
            return workerTask;
        }
    }
}
