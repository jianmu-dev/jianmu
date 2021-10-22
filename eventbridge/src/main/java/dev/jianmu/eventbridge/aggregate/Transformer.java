package dev.jianmu.eventbridge.aggregate;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

/**
 * @class: Transformer
 * @description: 变量转换器
 * @author: Ethan Liu
 * @create: 2021-08-11 14:34
 **/
public class Transformer {
    private String variableName;
    private String variableType;
    private String expression;

    public String getVariableName() {
        return variableName;
    }

    public String getVariableType() {
        return variableType;
    }

    public String getExpression() {
        return expression;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public void setVariableType(String variableType) {
        this.variableType = variableType;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public Object extractParameter(String payload) {
        Object document = Configuration.defaultConfiguration().jsonProvider().parse(payload);
        try {
            return JsonPath.read(document, this.getExpression());
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    public static final class Builder {
        private String variableName;
        private String variableType;
        private String expression;

        private Builder() {
        }

        public static Builder aTransformer() {
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

        public Transformer build() {
            Transformer transformer = new Transformer();
            transformer.variableType = this.variableType;
            transformer.expression = this.expression;
            transformer.variableName = this.variableName;
            return transformer;
        }
    }
}
