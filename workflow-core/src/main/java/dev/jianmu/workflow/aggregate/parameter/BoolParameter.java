package dev.jianmu.workflow.aggregate.parameter;

/**
 * @class BoolParameter
 * @description 布尔参数
 * @author Ethan Liu
 * @create 2021-04-20 22:51
*/
public class BoolParameter extends Parameter<Boolean> {
    public BoolParameter(Boolean value) {
        super(value);
        this.type = Type.BOOL;
    }

    public BoolParameter(Boolean value, boolean isDefault) {
        super(value, isDefault);
        this.type = Type.BOOL;
    }

    @Override
    public String getStringValue() {
        return String.valueOf(value);
    }
}
