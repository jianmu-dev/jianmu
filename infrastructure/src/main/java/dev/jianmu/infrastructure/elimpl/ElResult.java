package dev.jianmu.infrastructure.elimpl;


import dev.jianmu.workflow.aggregate.parameter.Parameter;
import dev.jianmu.workflow.el.EvaluationResult;

/**
 * @class: ElResult
 * @description: 表达式计算结果
 * @author: Ethan Liu
 * @create: 2021-03-08 22:02
 **/
public class ElResult implements EvaluationResult {

    private String expr;
    private Object result;

    public ElResult(String expr, Object result) {
        this.expr = expr;
        this.result = result;
    }

    @Override
    public String getExpression() {
        return this.expr;
    }

    @Override
    public boolean isFailure() {
        return false;
    }

    @Override
    public String getFailureMessage() {
        return "";
    }

    @Override
    public Parameter<?> getValue() {
        if (this.result instanceof Boolean) {
            return Parameter.Type.BOOL.newParameter(this.result);
        }
        if (this.result instanceof String) {
            return Parameter.Type.STRING.newParameter(this.result);
        }
        if (this.result instanceof Number) {
            return Parameter.Type.NUMBER.newParameter(this.result);
        }
        throw new RuntimeException("表达式计算结果类型: " + this.result.getClass() + "无法转换");
    }
}
