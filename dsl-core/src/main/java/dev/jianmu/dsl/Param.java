package dev.jianmu.dsl;

/**
 * @class: Param
 * @description: 参数类
 * @author: Ethan Liu
 * @create: 2021-04-21 17:59
 **/
public class Param {
    public enum Type {
        secret,
        bool,
        string,
        number,
        UNKNOWN
    }
    private Type type;
    private String value;

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Param{" +
                "type='" + type + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    public static final class Builder {
        private Type type;
        private String value;

        private Builder() {
        }

        public static Builder aParam() {
            return new Builder();
        }

        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Param build() {
            Param param = new Param();
            param.value = this.value;
            param.type = this.type;
            return param;
        }
    }
}
