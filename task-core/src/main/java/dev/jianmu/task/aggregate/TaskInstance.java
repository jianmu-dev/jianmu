package dev.jianmu.task.aggregate;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @class: TaskInstance
 * @description: 任务实例
 * @author: Ethan Liu
 * @create: 2021-03-25 15:44
 **/
public class TaskInstance extends AggregateRoot {
    // ID
    private String id;
    // 显示名称
    private String name;
    // 描述
    private String description;

    // 任务定义唯一Key
    private String defKey;
    // 任务定义版本
    private String defVersion;
    // 外部业务ID, 必须唯一
    private String businessId;
    // 触发器ID
    private String triggerId;
    // 开始时间
    private final LocalDateTime startTime = LocalDateTime.now();
    // 结束时间
    private LocalDateTime endTime;
    // 任务运行状态
    private InstanceStatus status = InstanceStatus.WAITING;

    // 输入输出参数列表
    private Set<TaskParameter> parameters = new HashSet<>();
    // Worker参数列表
    private Map<String, String> workerParameters = new HashMap<>();

    private TaskInstance() {
    }

    public Set<TaskParameter> getParameters() {
        return parameters;
    }

    public Map<String, String> getWorkerParameters() {
        return workerParameters;
    }

    public void setStatus(InstanceStatus status) {
        this.status = status;
        this.endTime = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDefKey() {
        return defKey;
    }

    public String getDefVersion() {
        return defVersion;
    }

    public String getBusinessId() {
        return businessId;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public InstanceStatus getStatus() {
        return status;
    }

    public static final class Builder {
        // ID
        // TODO 暂时使用UUID的值
        private String id = UUID.randomUUID().toString().replace("-", "");
        // 显示名称
        private String name;
        // 描述
        private String description;
        // 任务定义唯一Key
        private String defKey;
        // 任务定义版本
        private String defVersion;
        // 外部业务ID
        private String businessId;
        // 触发器ID
        private String triggerId;
        // 输入输出参数列表
        private Set<TaskParameter> parameters;
        // Worker参数列表
        private Map<String, String> workerParameters;

        private Builder() {
        }

        public static Builder anInstance() {
            return new Builder();
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder defKey(String defKey) {
            this.defKey = defKey;
            return this;
        }

        public Builder defVersion(String defVersion) {
            this.defVersion = defVersion;
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

        public Builder parameters(Set<TaskParameter> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder workerParameters(Map<String, String> workerParameters) {
            this.workerParameters = workerParameters;
            return this;
        }

        public TaskInstance build() {
            TaskInstance taskInstance = new TaskInstance();
            taskInstance.id = this.id;
            taskInstance.defKey = this.defKey;
            taskInstance.name = this.name;
            taskInstance.description = this.description;
            taskInstance.defVersion = this.defVersion;
            taskInstance.businessId = this.businessId;
            taskInstance.triggerId = this.triggerId;
            taskInstance.parameters = this.parameters;
            taskInstance.workerParameters = this.workerParameters;
            return taskInstance;
        }
    }
}
