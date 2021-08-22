package dev.jianmu.eventbridge.aggregate;

/**
 * @class: Transformer
 * @description: 变量转换器
 * @author: Ethan Liu
 * @create: 2021-08-11 14:34
 **/
public abstract class Transformer<T> {
    protected String variableName;
    protected String variableType;
    protected String expression;

    public String getVariableName() {
        return variableName;
    }

    public String getVariableType() {
        return variableType;
    }

    public String getExpression() {
        return expression;
    }

    public abstract T extractParameter(Payload payload);
}
