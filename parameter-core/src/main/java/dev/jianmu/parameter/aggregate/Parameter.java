package dev.jianmu.parameter.aggregate;

/**
 * @program: workflow
 * @description: 运行参数父类
 * @author: Ethan Liu
 * @create: 2021-01-21 13:13
 **/
public abstract class Parameter<T> {
    // 显示名称
    protected String name;

    // 唯一引用名称
    protected String ref;
    // 参数作用域
    protected Scope scope;

    // 描述
    protected String description;

    // 参数值来源
    protected Source source;

    // 参数值
    private final T value;

    protected Parameter(T value) {
        this.value = value;
    }

    // 获取参数值
    public T getValue() {
        return this.value;
    }

    public String getName() {
        return name;
    }

    public String getRef() {
        return ref;
    }

    public Scope getScope() {
        return scope;
    }

    public String getDescription() {
        return description;
    }
}
