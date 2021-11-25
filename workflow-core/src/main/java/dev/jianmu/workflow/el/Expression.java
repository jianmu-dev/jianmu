package dev.jianmu.workflow.el;

import java.util.Optional;

/**
 * @class Expression
 * @description A parsed expression.
 * @author Ethan Liu
 * @create 2021-01-30 19:40
*/
public interface Expression {
    /**
     * @return the (raw) expression as string
     */
    String getExpression();

    /**
     * @return optional of the name of the variable if expression is a single variable or a property
     * of a single variable, otherwise empty
     */
    Optional<String> getVariableName();

    /**
     * @return {@code true} if it is an static expression that does not require additional context
     * variables
     */
    boolean isStatic();

    /**
     * @return {@code true} if the expression is valid and can be evaluated
     */
    boolean isValid();

    /**
     * Returns the reason why the expression is not valid. Use {@link #isValid()} to check if the
     * expression is valid or not.
     *
     * @return the failure message if the expression is not valid, otherwise {@code null}
     */
    String getFailureMessage();
}
