package dev.jianmu.task.event;

/**
 * @class: CreateTaskInstanceEvent
 * @description: 任务实例运行事件
 * @author: Ethan Liu
 * @create: 2021-04-06 16:01
 **/
public class TaskInstanceRunningEvent extends BaseEvent {
    private TaskInstanceRunningEvent() {
    }

    public static final class Builder {
        // 任务实例ID
        protected String taskInstanceId;
        // 任务定义唯一Key
        protected String defKey;
        // 任务定义版本
        protected String defVersion;
        // 外部业务ID, 必须唯一
        protected String businessId;

        private Builder() {
        }

        public static Builder aTaskInstanceRunningEvent() {
            return new Builder();
        }

        public Builder taskInstanceId(String taskInstanceId) {
            this.taskInstanceId = taskInstanceId;
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

        public TaskInstanceRunningEvent build() {
            TaskInstanceRunningEvent taskInstanceRunningEvent = new TaskInstanceRunningEvent();
            taskInstanceRunningEvent.defKey = this.defKey;
            taskInstanceRunningEvent.defVersion = this.defVersion;
            taskInstanceRunningEvent.businessId = this.businessId;
            taskInstanceRunningEvent.taskInstanceId = this.taskInstanceId;
            return taskInstanceRunningEvent;
        }
    }
}
