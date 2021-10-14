package dev.jianmu.workflow.el;

/**
 * @class: EvaluationContext
 * @description: The context for evaluating an expression.
 * @author: Ethan Liu
 * @create: 2021-01-30 19:49
 **/
public interface EvaluationContext {
    /**
     * Returns the value of the variable with the given name.
     *
     * @param variableName the name of the variable
     * @return the variable value as Object, or {@code null} if the variable is
     * not present
     */
    Object getVariable(String variableName);
}
