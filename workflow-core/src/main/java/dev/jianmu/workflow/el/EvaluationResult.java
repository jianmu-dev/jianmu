package dev.jianmu.workflow.el;

import dev.jianmu.workflow.aggregate.parameter.Parameter;

/**
 * @class: EvaluationResult
 * @description: The result of an expression evaluation
 * @author: Ethan Liu
 * @create: 2021-01-30 10:31
 **/
public interface EvaluationResult {
    /**
     * @return the (raw) expression as string
     */
    String getExpression();

    /**
     * @return {@code true} if the evaluation was not successful
     */
    boolean isFailure();

    /**
     * Returns the reason why the evaluation failed. Use {@link #isFailure()} to check if the
     * evaluation failed or not.
     *
     * @return the failure message if the evaluation failed, otherwise {@code null}
     */
    String getFailureMessage();

    /**
     * @return the value of the evaluation result, or {@code null} if the evaluation failed
     */
    Parameter<?> getValue();
}
