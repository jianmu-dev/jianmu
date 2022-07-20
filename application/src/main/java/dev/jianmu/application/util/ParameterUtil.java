package dev.jianmu.application.util;

import dev.jianmu.workflow.aggregate.parameter.Parameter;

import java.math.BigDecimal;

public class ParameterUtil {

    public static Parameter<?> toParameter(String typeStr, String value) {
        var type = Parameter.Type.getTypeByName(typeStr);
        switch (type) {
            case BOOL:
                return type.newParameter(Boolean.valueOf(value));
            case NUMBER:
                return type.newParameter(new BigDecimal(value));
            default:
                return type.newParameter(value);
        }
    }
}
