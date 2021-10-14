package dev.jianmu.infrastructure.eventbridge;

import dev.jianmu.eventbridge.aggregate.Payload;
import dev.jianmu.eventbridge.aggregate.Transformer;
import dev.jianmu.workflow.aggregate.parameter.Parameter;

import java.util.Locale;

/**
 * @class: HeaderTransformer
 * @description: HeaderTransformer
 * @author: Ethan Liu
 * @create: 2021-08-15 14:44
 **/
public class HeaderTransformer extends Transformer<Parameter<?>> {
    @Override
    public Parameter<?> extractParameter(Payload payload) {
        var header = payload.getHeader();
        var list = header.get(this.getExpression().toLowerCase(Locale.ROOT));
        if (list != null && !list.isEmpty()) {
            var variable = String.join(",", list);
            return Parameter.Type.getTypeByName(this.getVariableType()).newParameter(variable);
        }
        return Parameter.Type.getTypeByName(this.getVariableType()).defaultParameter();
    }

    public static final class Builder {
        protected String variableName;
        protected String variableType;
        protected String expression;

        private Builder() {
        }

        public static Builder aHeaderTransformer() {
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

        public HeaderTransformer build() {
            HeaderTransformer headerTransformer = new HeaderTransformer();
            headerTransformer.expression = this.expression;
            headerTransformer.variableName = this.variableName;
            headerTransformer.variableType = this.variableType;
            return headerTransformer;
        }
    }
}
