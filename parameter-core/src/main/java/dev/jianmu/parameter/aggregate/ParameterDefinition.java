package dev.jianmu.parameter.aggregate;

/**
 * @class: Definition
 * @description: 参数定义
 * @author: Ethan Liu
 * @create: 2021-03-25 19:19
 **/
public abstract class ParameterDefinition<T> {
    protected ParameterDefinition(T value) {
        this.value = value;
    }

    public enum Source {
        EXTERNAL,
        INTERNAL
    }

    public enum Type {
        IMMUTABLE,
        MUTABLE
    }

    // 显示名称
    protected String name;
    // 唯一引用名称
    protected String ref;
    // 描述
    protected String description;
    // 外部ID, WorkflowId or TaskDefinitionId or WorkerId
    protected String businessId;
    // 作用域
    protected String scope;
    // 来源
    protected Source source;
    // 类型
    protected Type type;
    // 参数值
    private final T value;

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

    public Source getSource() {
        return source;
    }

    public Type getType() {
        return type;
    }

    public T getValue() {
        return value;
    }
}
