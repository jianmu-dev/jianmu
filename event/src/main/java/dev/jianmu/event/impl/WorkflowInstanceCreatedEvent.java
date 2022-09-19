package dev.jianmu.event.impl;

/**
 * @author Daihw
 * @class WorkflowInstanceCreatedEvent
 * @description 流程实例创建事件
 * @create 2022/9/19 2:14 下午
 */
public class WorkflowInstanceCreatedEvent extends BaseEvent {
    private String id;
    private String triggerId;
    private String triggerType;
    private int serialNo;
    private String name;
    private String workflowRef;
    private String workflowVersion;
    private String description;
    private String status;

    public String getId() {
        return id;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public int getSerialNo() {
        return serialNo;
    }

    public String getName() {
        return name;
    }

    public String getWorkflowRef() {
        return workflowRef;
    }

    public String getWorkflowVersion() {
        return workflowVersion;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public static class Builder {
        private String id;
        private String triggerId;
        private String triggerType;
        private int serialNo;
        private String name;
        private String workflowRef;
        private String workflowVersion;
        private String description;
        private String status;

        public static Builder aWorkflowInstanceCreatedEvent() {
            return new Builder();
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder triggerId(String triggerId) {
            this.triggerId = triggerId;
            return this;
        }

        public Builder triggerType(String triggerType) {
            this.triggerType = triggerType;
            return this;
        }

        public Builder serialNo(int serialNo) {
            this.serialNo = serialNo;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder workflowRef(String workflowRef) {
            this.workflowRef = workflowRef;
            return this;
        }

        public Builder workflowVersion(String workflowVersion) {
            this.workflowVersion = workflowVersion;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public WorkflowInstanceCreatedEvent build() {
            var event = new WorkflowInstanceCreatedEvent();
            event.id = this.id;
            event.triggerId = this.triggerId;
            event.triggerType = this.triggerType;
            event.serialNo = this.serialNo;
            event.name = this.name;
            event.workflowRef = this.workflowRef;
            event.workflowVersion = this.workflowVersion;
            event.description = this.description;
            event.status = this.status;
            return event;
        }
    }
}
