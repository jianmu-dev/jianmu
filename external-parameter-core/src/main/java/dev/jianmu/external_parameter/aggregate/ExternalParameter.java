package dev.jianmu.external_parameter.aggregate;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author huangxi
 * @class Parameter
 * @description Parameter
 * @create 2022-07-13 10:48
 */
public class ExternalParameter extends BaseAssociation {
    /**
     * 创建时间
     */
    private final LocalDateTime createdTime = LocalDateTime.now();
    /**
     * ID
     */
    private String id;
    /**
     * 唯一标识
     */
    private String ref;
    /**
     * 参数名
     */
    private String name;
    /**
     * 参数类型
     */
    private Type type;
    /**
     * 参数值
     */
    private String value;
    /**
     * 标签
     */
    private String label;
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

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * 参数类型
     */
    public enum Type {
        BOOL, STRING, NUMBER
    }

    public static class Builder {
        private String ref;
        private String name;
        private Type type;
        private String value;
        private String label;
        private String associationId;
        private String associationType;
        private String associationPlatform;

        public Builder() {
        }

        public static Builder aReference() {
            return new Builder();
        }

        public Builder ref(String ref) {
            this.ref = ref;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder associationId(String associationId) {
            this.associationId = associationId;
            return this;
        }

        public Builder associationType(String associationType) {
            this.associationType = associationType;
            return this;
        }

        public Builder associationPlatform(String associationPlatform) {
            this.associationPlatform = associationPlatform;
            return this;
        }

        public ExternalParameter build() {
            var parameter = new ExternalParameter();
            parameter.id = UUID.randomUUID().toString().replace("-", "");
            parameter.name = this.name;
            parameter.value = this.value;
            parameter.ref = this.ref;
            parameter.type = this.type;
            parameter.label = this.label;
            parameter.lastModifiedTime = LocalDateTime.now();
            parameter.updateAssociation(this.associationId, this.associationType, this.associationPlatform);
            return parameter;
        }
    }
}
