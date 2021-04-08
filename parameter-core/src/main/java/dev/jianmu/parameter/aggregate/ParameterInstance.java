package dev.jianmu.parameter.aggregate;

/**
 * @class: Instance
 * @description: 参数实例
 * @author: Ethan Liu
 * @create: 2021-04-04 10:16
 **/
public abstract class ParameterInstance<T> {
    // 显示名称
    protected String name;
    // 唯一引用名称
    protected String ref;
    // 描述
    protected String description;
    // 外部ID, WorkflowInstanceId or TaskInstanceId
    protected String businessId;
    // 作用域
    protected String scope;
    // 类型
    protected ParameterDefinition.Type type;
    // 参数值
    private final T value;

    protected ParameterInstance(T value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getRef() {
        return ref;
    }

    public String getDescription() {
        return description;
    }

    public String getBusinessId() {
        return businessId;
    }

    public String getScope() {
        return scope;
    }

    public ParameterDefinition.Type getType() {
        return type;
    }

    public T getValue() {
        return value;
    }
}
