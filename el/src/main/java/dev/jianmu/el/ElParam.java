package dev.jianmu.el;

/**
 * @class ParamVo
 * @description ParamVo
 * @author Daihw
 * @create 2022/7/15 4:44 下午
 */
public class ElParam {
    private String key;
    private String type;
    private Object value;

    public ElParam() {
    }

    public String getKey() {
        return key;
    }

    public String getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public static Builder aParamVo() {
        return new Builder();
    }

    public static class Builder {
        private String key;
        private String type;
        private Object value;

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder value(Object value) {
            this.value = value;
            return this;
        }

        public ElParam build() {
            var paramVo = new ElParam();
            paramVo.key = this.key;
            paramVo.type = this.type;
            paramVo.value = this.value;
            return paramVo;
        }
    }
}
