package dev.jianmu.workflow.aggregate.definition;

import dev.jianmu.workflow.el.EvaluationContext;
import dev.jianmu.workflow.el.ExpressionLanguage;

/**
 * @class Gateway
 * @description 流程网关类型
 * @author Ethan Liu
 * @create 2021-03-16 10:17
*/
public interface Gateway {
    String calculateTarget(ExpressionLanguage expressionLanguage, EvaluationContext context);
}
