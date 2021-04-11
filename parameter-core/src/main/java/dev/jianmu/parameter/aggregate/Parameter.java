package dev.jianmu.parameter.aggregate;

import java.util.UUID;

/**
 * @program: Parameter
 * @description: 参数类
 * @author: Ethan Liu
 * @create: 2021-01-21 13:13
 **/
public class Parameter {
    // ID
    private String id;
    // 参数类型
    private String type;
    // 参数值
    private String value;

    private Parameter() {
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }


    public static final class Builder {
        // ID
        // TODO 暂时使用UUID的值
        private final String id = UUID.randomUUID().toString().replace("-", "");
        // 参数类型
        private String type;
        // 参数值
        private String value;

        private Builder() {
        }

        public static Builder aParameter() {
            return new Builder();
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Builder type(String type) {
            if (!type.equals("String")) {
                throw new RuntimeException("当前不支持此参数类型: "+ type);
            }
            this.type = type;
            return this;
        }

        public Parameter build() {
            Parameter parameter = new Parameter();
            parameter.id = this.id;
            parameter.type = this.type;
            parameter.value = this.value;
            return parameter;
        }
    }
}
