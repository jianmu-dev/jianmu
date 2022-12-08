package dev.jianmu.task.event;

/**
 * @class CreateTaskInstanceEvent
 * @description 任务实例运行事件
 * @author Ethan Liu
 * @create 2021-04-06 16:01
*/
public class TaskInstanceWaitingEvent extends BaseEvent {
    private TaskInstanceWaitingEvent() {
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

        public TaskInstanceWaitingEvent build() {
            TaskInstanceWaitingEvent taskInstanceRunningEvent = new TaskInstanceWaitingEvent();
            taskInstanceRunningEvent.defKey = this.defKey;
            taskInstanceRunningEvent.triggerId = this.triggerId;
            taskInstanceRunningEvent.businessId = this.businessId;
            taskInstanceRunningEvent.taskInstanceId = this.taskInstanceId;
            taskInstanceRunningEvent.asyncTaskRef = this.asyncTaskRef;
            return taskInstanceRunningEvent;
        }
    }
}
