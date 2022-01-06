package dev.jianmu.workflow.aggregate.definition;

import dev.jianmu.workflow.aggregate.parameter.Parameter;

/**
 * @author Ethan Liu
 * @class TaskParameter
 * @description 任务参数
 * @create 2021-09-04 17:10
 */
public class TaskParameter {
    private String ref;
    private Parameter.Type type;
    private String expression;

    public String getRef() {
        return ref;
    }

    public Parameter.Type getType() {
        return type;
    }

    public String getExpression() {
        return expression;
    }

    public static final class Builder {
        private String ref;
        private Parameter.Type type;
        private String expression;

        private Builder() {
        }

        public static Builder aTaskParameter() {
            return new Builder();
        }

        public Builder ref(String ref) {
            this.ref = ref;
            return this;
        }

        public Builder type(Parameter.Type type) {
            this.type = type;
            return this;
        }

        public Builder expression(String expression) {
            this.expression = expression;
            return this;
        }

        public TaskParameter build() {
            TaskParameter taskParameter = new TaskParameter();
            taskParameter.expression = this.expression;
            taskParameter.type = this.type;
            taskParameter.ref = this.ref;
            return taskParameter;
        }
    }
}
