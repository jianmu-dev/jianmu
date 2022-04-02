package dev.jianmu.workflow.event.definition;

/**
 * @author Ethan Liu
 * @class AsyncTaskActivatingEvent
 * @description 异步任务节点激活事件
 * @create 2021-12-31 22:55
 */
public class AsyncTaskActivatingEvent extends DefinitionEvent {
    // 乐观锁版本
    private int version;

    public int getVersion() {
        return version;
    }

    private AsyncTaskActivatingEvent() {
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
        // 乐观锁版本
        private int version;

        private Builder() {
        }

        public static Builder anAsyncTaskActivatingEvent() {
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

        public Builder version(int version) {
            this.version = version;
            return this;
        }

        public AsyncTaskActivatingEvent build() {
            AsyncTaskActivatingEvent asyncTaskActivatingEvent = new AsyncTaskActivatingEvent();
            asyncTaskActivatingEvent.workflowVersion = this.workflowVersion;
            asyncTaskActivatingEvent.nodeType = this.nodeType;
            asyncTaskActivatingEvent.workflowRef = this.workflowRef;
            asyncTaskActivatingEvent.triggerId = this.triggerId;
            asyncTaskActivatingEvent.nodeRef = this.nodeRef;
            asyncTaskActivatingEvent.version = this.version;
            return asyncTaskActivatingEvent;
        }
    }
}
