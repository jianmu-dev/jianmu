package dev.jianmu.workflow.event.definition;

/**
 * @author Ethan Liu
 * @class AsyncTaskSkipEvent
 * @description 异步任务跳过事件
 * @create 2022-02-21 14:28
 */
public class AsyncTaskSkipEvent extends DefinitionEvent {
    private AsyncTaskSkipEvent() {
    }

    public static final class Builder {
        // 流程定义唯一引用名称
        protected String workflowRef;
        // 流程定义版本
        protected String workflowVersion;
        // 触发器ID
        protected String triggerId;
        // 节点唯一引用名称
        protected String nodeRef;
        // 节点类型
        protected String nodeType;

        private Builder() {
        }

        public static Builder anAsyncTaskSkipEvent() {
            return new Builder();
        }

        public Builder workflowRef(String workflowRef) {
            this.workflowRef = workflowRef;
            return this;
        }

        public Builder workflowVersion(String workflowVersion) {
            this.workflowVersion = workflowVersion;
            return this;
        }

        public Builder triggerId(String triggerId) {
            this.triggerId = triggerId;
            return this;
        }

        public Builder nodeRef(String nodeRef) {
            this.nodeRef = nodeRef;
            return this;
        }

        public Builder nodeType(String nodeType) {
            this.nodeType = nodeType;
            return this;
        }

        public AsyncTaskSkipEvent build() {
            AsyncTaskSkipEvent asyncTaskSkipEvent = new AsyncTaskSkipEvent();
            asyncTaskSkipEvent.nodeRef = this.nodeRef;
            asyncTaskSkipEvent.workflowRef = this.workflowRef;
            asyncTaskSkipEvent.workflowVersion = this.workflowVersion;
            asyncTaskSkipEvent.triggerId = this.triggerId;
            asyncTaskSkipEvent.nodeType = this.nodeType;
            return asyncTaskSkipEvent;
        }
    }
}
