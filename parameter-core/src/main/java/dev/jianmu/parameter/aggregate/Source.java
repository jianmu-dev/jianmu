package dev.jianmu.parameter.aggregate;

/**
 * @class: Source
 * @description: 参数来源
 * @author: Ethan Liu
 * @create: 2021-02-11 17:01
 **/
public class Source {
    private Type type;
    private String value = "";

    /**
     *@title: 来源类型
     *@description:   EXTERNAL为外部传入，EXP为表达式求值计算出的值
     *@author: ethan-liu
     *@date: 2021-02-11 17:08
     *@param: null
     *@return:
     *@throws:
     */
    public enum Type {
        EXTERNAL,
        INTERNAL,
        EXP
    }

    private Source() {
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }


    public static final class Builder {
        private Type type;
        private String value = "";

        private Builder() {
        }

        public static Builder aSource() {
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

        public Source build() {
            Source source = new Source();
            source.type = this.type;
            source.value = this.value;
            return source;
        }
    }
}
