package dev.jianmu.infrastructure.eventbridge;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import dev.jianmu.eventbridge.aggregate.Payload;
import dev.jianmu.eventbridge.aggregate.Transformer;
import dev.jianmu.parameter.aggregate.Parameter;

/**
 * @class: BodyTransformer
 * @description: BodyTransformer
 * @author: Ethan Liu
 * @create: 2021-08-15 14:11
 **/
public class BodyTransformer extends Transformer<Parameter<?>> {
    @Override
    public Parameter<?> extractParameter(Payload payload) {
        Object document = Configuration.defaultConfiguration().jsonProvider().parse(payload.getBody());
        try {
            var variable = JsonPath.read(document, this.getExpression());
            return Parameter.Type.valueOf(this.getVariableType()).newParameter(variable);
        } catch (PathNotFoundException e) {
            return Parameter.Type.valueOf(this.getVariableType()).defaultParameter();
        }
    }

    public static final class Builder {
        protected String variableName;
        protected String variableType;
        protected String expression;

        private Builder() {
        }

        public static Builder aBodyTransformer() {
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

        public BodyTransformer build() {
            BodyTransformer bodyTransformer = new BodyTransformer();
            bodyTransformer.expression = this.expression;
            bodyTransformer.variableName = this.variableName;
            bodyTransformer.variableType = this.variableType;
            return bodyTransformer;
        }
    }
}
