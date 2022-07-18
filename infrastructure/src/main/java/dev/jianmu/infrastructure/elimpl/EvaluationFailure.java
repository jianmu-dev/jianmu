package dev.jianmu.infrastructure.elimpl;

import dev.jianmu.workflow.aggregate.parameter.Parameter;
import dev.jianmu.workflow.el.EvaluationResult;

/**
 * @class EvaluationFailure
 * @description 失败结果
 * @author Ethan Liu
 * @create 2021-03-08 22:35
*/
public class EvaluationFailure implements EvaluationResult {

    private final String expression;
    private final String failureMessage;

    public EvaluationFailure(final String expression, final String failureMessage) {
        this.expression = expression;
        this.failureMessage = failureMessage;
    }

    @Override
    public String getExpression() {
        return this.expression;
    }

    @Override
    public boolean isFailure() {
        return true;
    }

    @Override
    public String getFailureMessage() {
        return this.failureMessage;
    }

    @Override
    public Parameter<?> getValue() {
        return null;
    }


}
