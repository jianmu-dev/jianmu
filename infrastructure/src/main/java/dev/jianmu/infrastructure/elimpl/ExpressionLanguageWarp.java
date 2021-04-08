package dev.jianmu.infrastructure.elimpl;

import dev.jianmu.el.El;
import dev.jianmu.workflow.el.EvaluationContext;
import dev.jianmu.workflow.el.EvaluationResult;
import dev.jianmu.workflow.el.Expression;
import dev.jianmu.workflow.el.ExpressionLanguage;
import org.springframework.stereotype.Service;

/**
 * @class: ExpressionLanguageWarp
 * @description: 表达式引擎包装类
 * @author: Ethan Liu
 * @create: 2021-02-21 11:08
 **/
@Service
public class ExpressionLanguageWarp implements ExpressionLanguage {
    @Override
    public Expression parseExpression(String expression) {
        return new El(expression);
    }

    @Override
    public EvaluationResult evaluateExpression(Expression expression, EvaluationContext context) {
        try {
            Object rawResult = ((El) expression).eval(context);
            return new ElResult(expression.getExpression(), rawResult);
        } catch (Exception e) {
            e.printStackTrace();
            return new EvaluationFailure(expression.getExpression(), e.getMessage());
        }
    }
}
