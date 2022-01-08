package dev.jianmu.task.event;

/**
 * @author Ethan Liu
 * @class TaskInstanceCreatedEvent
 * @description 任务实例创建事件
 * @create 2022-01-06 18:37
 */
public class TaskInstanceCreatedEvent extends BaseEvent {
    private TaskInstanceCreatedEvent() {
    }

    public static final class Builder {
        // 任务实例ID
        protected String taskInstanceId;
        // 任务定义唯一Key
        protected String defKey;
        // 流程定义上下文中的AsyncTask唯一标识
        protected String asyncTaskRef;
        // 外部触发ID，流程实例唯一
        protected String triggerId;
        // 外部业务ID, 必须唯一
        protected String businessId;

        private Builder() {
        }

        public static Builder aTaskInstanceCreatedEvent() {
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

        public Builder asyncTaskRef(String asyncTaskRef) {
            this.asyncTaskRef = asyncTaskRef;
            return this;
        }

        public Builder triggerId(String triggerId) {
            this.triggerId = triggerId;
            return this;
        }

        public Builder businessId(String businessId) {
            this.businessId = businessId;
            return this;
        }

        public TaskInstanceCreatedEvent build() {
            TaskInstanceCreatedEvent taskInstanceCreatedEvent = new TaskInstanceCreatedEvent();
            taskInstanceCreatedEvent.defKey = this.defKey;
            taskInstanceCreatedEvent.triggerId = this.triggerId;
            taskInstanceCreatedEvent.businessId = this.businessId;
            taskInstanceCreatedEvent.taskInstanceId = this.taskInstanceId;
            taskInstanceCreatedEvent.asyncTaskRef = this.asyncTaskRef;
            return taskInstanceCreatedEvent;
        }
    }
}
