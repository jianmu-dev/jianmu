package dev.jianmu.infrastructure.eventbridge;

import dev.jianmu.eventbridge.aggregate.Payload;
import dev.jianmu.eventbridge.aggregate.Transformer;
import dev.jianmu.parameter.aggregate.Parameter;

/**
 * @class: QueryTransformer
 * @description: QueryTransformer
 * @author: Ethan Liu
 * @create: 2021-08-15 15:40
 **/
public class QueryTransformer extends Transformer<Parameter<?>> {
    @Override
    public Parameter<?> extractParameter(Payload payload) {
        var query = payload.getQuery();
        var strings = query.get(this.getExpression());
        if (strings != null && strings.length > 0) {
            var variable = String.join(",", strings);
            return Parameter.Type.valueOf(this.getVariableType()).newParameter(variable);
        }
        return Parameter.Type.valueOf(this.getVariableType()).defaultParameter();
    }

    public static final class Builder {
        protected String variableName;
        protected String variableType;
        protected String expression;

        private Builder() {
        }

        public static Builder aQueryTransformer() {
            return new Builder();
        }

        public Builder variableName(String variableName) {
            this.variableName = variableName;
            return this;
        }

        public Builder variableType(String variableType) {
            this.variableType = variableType;
            return this;
        }

        public Builder expression(String expression) {
            this.expression = expression;
            return this;
        }

        public QueryTransformer build() {
            QueryTransformer queryTransformer = new QueryTransformer();
            queryTransformer.expression = this.expression;
            queryTransformer.variableName = this.variableName;
            queryTransformer.variableType = this.variableType;
            return queryTransformer;
        }
    }
}
