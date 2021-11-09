package dev.jianmu.workflow.aggregate.definition;

/**
 * @class: TaskParameter
 * @description: 任务参数
 * @author: Ethan Liu
 * @create: 2021-09-04 17:10
 **/
public class TaskParameter {
    private String ref;
    private String expression;

    public String getRef() {
        return ref;
    }

    public String getExpression() {
        return expression;
    }

    public static final class Builder {
        private String ref;
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

        public Builder expression(String expression) {
            this.expression = expression;
            return this;
        }

        public TaskParameter build() {
            TaskParameter taskParameter = new TaskParameter();
            taskParameter.expression = this.expression;
            taskParameter.ref = this.ref;
            return taskParameter;
        }
    }
}
