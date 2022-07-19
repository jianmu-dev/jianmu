package dev.jianmu.el;

import dev.jianmu.workflow.el.EvaluationContext;
import dev.jianmu.workflow.el.Expression;
import dev.jianmu.workflow.el.ResultType;

import java.util.Optional;

/**
 * @author laoji
 * @class El
 * @description 表达式引擎入口类
 * @create 2022-06-30 10:23
 */
public class El implements Expression {
    private final String expr;
    private final boolean valid;
    private final ResultType resultType;

    public El(String expr, ResultType resultType) {
        this.expr = expr;
        this.valid = true;
        this.resultType = resultType;
    }

    @Override
    public ResultType getResultType() {
        return resultType;
    }

    @Override
    public String getExpression() {
        return this.expr;
    }

    @Override
    public Optional<String> getVariableName() {
        return Optional.empty();
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public boolean isValid() {
        return this.valid;
    }

    @Override
    public String getFailureMessage() {
        return null;
    }
}
