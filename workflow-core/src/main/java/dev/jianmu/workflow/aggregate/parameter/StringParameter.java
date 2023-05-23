package dev.jianmu.workflow.aggregate.parameter;

/**
 * @class StringParameter
 * @description 字符串参数
 * @author Ethan Liu
 * @create 2021-04-20 22:49
*/
public class StringParameter extends Parameter<String> {
    public StringParameter(String value) {
        super(value);
        this.type = Type.STRING;
    }

    public StringParameter(String value, boolean isDefault) {
        super(value, isDefault);
        this.type = Type.STRING;
    }

    @Override
    public String getStringValue() {
        return value;
    }
}
