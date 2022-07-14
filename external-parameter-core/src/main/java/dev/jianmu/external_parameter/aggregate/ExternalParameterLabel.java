package dev.jianmu.external_parameter.aggregate;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author huangxi
 * @class ParameterLabel
 * @description ParameterLabel
 * @create 2022-07-13 11:40
 */
public class ExternalParameterLabel extends BaseAssociation {
    /**
     * 创建时间
     */
    private final LocalDateTime createdTime = LocalDateTime.now();
    /**
     * ID
     */
    private String id;
    /**
     * 标签值
     */
    private String value;
    /**
     * 最后修改时间
     */
    private LocalDateTime lastModifiedTime;


    public void setLastModifiedTime() {
        this.lastModifiedTime = LocalDateTime.now();
    }

    public LocalDateTime getLastModifiedTime() {
        return lastModifiedTime;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

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
        private String associationId;
        private String associationType;

        public Builder() {
        }

        public static Builder aReference() {
            return new Builder();
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public ExternalParameterLabel.Builder associationId(String associationId) {
            this.associationId = associationId;
            return this;
        }

        public ExternalParameterLabel.Builder associationType(String associationType) {
            this.associationType = associationType;
            return this;
        }

        public ExternalParameterLabel build() {
            var parameterLabel = new ExternalParameterLabel();
            parameterLabel.id = UUID.randomUUID().toString().replace("-", "");
            parameterLabel.value = this.value;
            parameterLabel.lastModifiedTime = LocalDateTime.now();
            parameterLabel.setAssociationId(this.associationId);
            parameterLabel.setAssociationType(this.associationType);
            return parameterLabel;
        }
    }

}
