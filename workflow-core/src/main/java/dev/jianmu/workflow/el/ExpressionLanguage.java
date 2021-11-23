package dev.jianmu.workflow.el;

/**
 * @class ExpressionLanguage
 * @description A parser and interpreter for the expression language. An expression can be parsed and stored as
 * object. The parsed expression needs to be used to evaluate the expression with a given variable context.
 * @author Ethan Liu
 * @create 2021-01-30 19:46
*/
public interface ExpressionLanguage {
    /**
     * Parse the given string into an expression.
     *
     * @param expression the (raw) expression as string
     * @return the parsed expression, or the failure message if the expression is not valid
     */
    Expression parseExpression(String expression);

    /**
     * Evaluate the given expression with the given context variables.
     *
     * @param expression the parsed expression
     * @param context    the context to evaluate the expression with
     * @return the result of the evaluation, or the failure message if the evaluation was not
     * successful
     */
    EvaluationResult evaluateExpression(Expression expression, EvaluationContext context);
}
