package dev.jianmu.externalParameter.aggregate;

import java.util.UUID;

/**
 * @author huangxi
 * @class ParameterLabel
 * @description ParameterLabel
 * @create 2022-07-13 11:40
 */
public class ExternalParameterLabel {
    /**
     * ID
     */
    private String id;

    /**
     * 标签值
     */
    private String value;

    public String getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static class Builder {
        private String value;

        public Builder() {
        }

        public static Builder aReference() {
            return new Builder();
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public ExternalParameterLabel build() {
            var parameterLabel = new ExternalParameterLabel();
            parameterLabel.id = UUID.randomUUID().toString().replace("-", "");
            parameterLabel.value = this.value;
            return parameterLabel;
        }
    }

}
