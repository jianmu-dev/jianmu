package dev.jianmu.parameter.aggregate;

/**
 * @class: Scope
 * @description: 作用域类型
 * @author: Ethan Liu
 * @create: 2021-02-06 11:04
 **/
public enum Category {
    WORKFLOW_INPUT,
    WORKFLOW_OUTPUT,
    NODE_INPUT,
    NODE_OUTPUT,
    WORKFLOWINSTANCE_INPUT,
    WORKFLOWINSTANCE_OUTPUT,
    TASK_INPUT,
    TASK_OUTPUT
}
