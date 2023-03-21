package dev.jianmu.task.aggregate;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @class TaskInstance
 * @description 任务实例
 * @author Ethan Liu
 * @create 2021-03-25 15:44
*/
public class TaskInstance extends AggregateRoot {
    // ID
    private String id;
    // 执行顺序号
    private int serialNo;
    // 任务定义Key, 表示任务定义类型
    private String defKey;
    // 节点定义快照
    private NodeInfo nodeInfo;
    // 流程定义上下文中的AsyncTask唯一标识
    private String asyncTaskRef;
    // 流程定义Ref
    private String workflowRef;
    // 流程定义版本
    private String workflowVersion;
    // 外部业务ID, 必须唯一
    private String businessId;
    // 外部触发ID，流程实例唯一
    private String triggerId;
    // 开始时间
    private final LocalDateTime startTime = LocalDateTime.now();
    // 结束时间
    private LocalDateTime endTime;
    // 任务运行状态
    private InstanceStatus status = InstanceStatus.INIT;
    // workerId
    private String workerId;
    // version
    private int version;

    private TaskInstance() {
    }

    public void waiting() {
        this.status = InstanceStatus.WAITING;
    }

    public void running() {
        this.status = InstanceStatus.RUNNING;
    }

    public void executeSucceeded() {
        this.status = InstanceStatus.EXECUTION_SUCCEEDED;
        this.endTime = LocalDateTime.now();
    }

    public void executeFailed() {
        if (this.status == InstanceStatus.EXECUTION_FAILED) {
            return;
        }
        this.status = InstanceStatus.EXECUTION_FAILED;
        this.endTime = LocalDateTime.now();
    }

    public void dispatchFailed() {
        this.status = InstanceStatus.DISPATCH_FAILED;
        this.endTime = LocalDateTime.now();
    }

    public void acceptTask(int version) {
        this.version = version;
        this.endTime = LocalDateTime.now();
    }

    public boolean isVolume() {
        return this.defKey.equals("start") || this.defKey.equals("end");
    }

    public boolean isCreationVolume() {
        return this.defKey.equals("start");
    }

    public boolean isDeletionVolume() {
        return this.defKey.equals("end");
    }

    public boolean isCache() {
        return this.asyncTaskRef.equals("cache");
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getId() {
        return id;
    }

    public int getSerialNo() {
        return serialNo;
    }

    public String getDefKey() {
        return defKey;
    }

    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    public String getAsyncTaskRef() {
        return asyncTaskRef;
    }

    public String getWorkflowRef() {
        return workflowRef;
    }

    public String getWorkflowVersion() {
        return workflowVersion;
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

    public String getWorkerId() {
        return workerId;
    }

    public Integer getVersion() {
        return version;
    }

    public static final class Builder {
        // ID
        // TODO 暂时使用UUID的值
        private String id = UUID.randomUUID().toString().replace("-", "");
        // 执行顺序号
        private int serialNo;
        // 任务定义唯一Key
        private String defKey;
        // 节点定义快照
        private NodeInfo nodeInfo;
        // 流程定义上下文中的AsyncTask唯一标识
        private String asyncTaskRef;
        // 流程定义Ref
        private String workflowRef;
        // 流程定义版本
        private String workflowVersion;
        // 外部业务ID
        private String businessId;
        // 外部触发ID，流程实例唯一
        private String triggerId;

        private Builder() {
        }

        public static Builder anInstance() {
            return new Builder();
        }

        public Builder serialNo(int serialNo) {
            this.serialNo = serialNo;
            return this;
        }

        public Builder defKey(String defKey) {
            this.defKey = defKey;
            return this;
        }

        public Builder nodeInfo(NodeInfo nodeInfo) {
            this.nodeInfo = nodeInfo;
            return this;
        }

        public Builder asyncTaskRef(String asyncTaskRef) {
            this.asyncTaskRef = asyncTaskRef;
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

        public Builder businessId(String businessId) {
            this.businessId = businessId;
            return this;
        }

        public Builder triggerId(String triggerId) {
            this.triggerId = triggerId;
            return this;
        }

        public TaskInstance build() {
            TaskInstance taskInstance = new TaskInstance();
            taskInstance.id = this.id;
            taskInstance.serialNo = this.serialNo;
            taskInstance.defKey = this.defKey;
            taskInstance.nodeInfo = this.nodeInfo;
            taskInstance.asyncTaskRef = this.asyncTaskRef;
            taskInstance.workflowRef = this.workflowRef;
            taskInstance.workflowVersion = this.workflowVersion;
            taskInstance.businessId = this.businessId;
            taskInstance.triggerId = this.triggerId;
            return taskInstance;
        }
    }
}
