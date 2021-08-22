package dev.jianmu.task.aggregate;

/**
 * @class: InstanceParameter
 * @description: 任务实例参数
 * @author: Ethan Liu
 * @create: 2021-04-28 14:13
 **/
public class InstanceParameter {
    public enum Type {
        INPUT,
        OUTPUT
    }

    // 任务实例ID
    private String instanceId;
    // 执行顺序号
    private int serialNo;
    // 任务定义Key, 表示任务定义类型
    private String defKey;
    // 流程定义上下文中的AsyncTask唯一标识
    private String asyncTaskRef;
    // 外部业务ID, 必须唯一
    private String businessId;
    // 外部触发ID，流程实例唯一
    private String triggerId;
    // 参数唯一引用名称
    private String ref;
    // 输入输出类型
    private Type type;
    // 参数引用Id
    private String parameterId;

    public String getInstanceId() {
        return instanceId;
    }

    public int getSerialNo() {
        return serialNo;
    }

    public String getDefKey() {
        return defKey;
    }

    public String getAsyncTaskRef() {
        return asyncTaskRef;
    }

    public String getBusinessId() {
        return businessId;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public String getRef() {
        return ref;
    }

    public Type getType() {
        return type;
    }

    public String getParameterId() {
        return parameterId;
    }

    public static final class Builder {
        // 任务实例ID
        private String instanceId;
        // 执行顺序号
        private int serialNo;
        // 任务定义Key, 表示任务定义类型
        private String defKey;
        // 流程定义上下文中的AsyncTask唯一标识
        private String asyncTaskRef;
        // 外部业务ID, 必须唯一
        private String businessId;
        // 外部触发ID，流程实例唯一
        private String triggerId;
        // 参数唯一引用名称
        private String ref;
        // 输入输出类型
        private Type type;
        // 参数引用Id
        private String parameterId;

        private Builder() {
        }

        public static Builder anInstanceParameter() {
            return new Builder();
        }

        public Builder instanceId(String instanceId) {
            this.instanceId = instanceId;
            return this;
        }

        public Builder serialNo(int serialNo) {
            this.serialNo = serialNo;
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

        public Builder businessId(String businessId) {
            this.businessId = businessId;
            return this;
        }

        public Builder triggerId(String triggerId) {
            this.triggerId = triggerId;
            return this;
        }

        public Builder ref(String ref) {
            this.ref = ref;
            return this;
        }

        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        public Builder parameterId(String parameterId) {
            this.parameterId = parameterId;
            return this;
        }

        public InstanceParameter build() {
            InstanceParameter instanceParameter = new InstanceParameter();
            instanceParameter.triggerId = this.triggerId;
            instanceParameter.type = this.type;
            instanceParameter.asyncTaskRef = this.asyncTaskRef;
            instanceParameter.parameterId = this.parameterId;
            instanceParameter.defKey = this.defKey;
            instanceParameter.instanceId = this.instanceId;
            instanceParameter.serialNo = this.serialNo;
            instanceParameter.ref = this.ref;
            instanceParameter.businessId = this.businessId;
            return instanceParameter;
        }
    }
}
