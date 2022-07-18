package dev.jianmu.secret.aggregate;

/**
 * @author Ethan Liu
 * @class KVPair
 * @description 键值对
 * @create 2021-04-20 12:40
 */
public class KVPair extends BaseAssociation {
    private String namespaceName;
    private String key;
    private String value;

    public String getNamespaceName() {
        return namespaceName;
    }

    public void setNamespaceName(String namespaceName) {
        this.namespaceName = namespaceName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static final class Builder {
        private String namespaceName;
        private String key;
        private String value;
        private String associationId;
        private String associationType;

        private Builder() {
        }

        public static Builder aKVPair() {
            return new Builder();
        }

        public Builder namespaceName(String namespaceName) {
            this.namespaceName = namespaceName;
            return this;
        }

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder value(String value) {
            this.value = value;
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

        public KVPair build() {
            KVPair kVPair = new KVPair();
            kVPair.setNamespaceName(namespaceName);
            kVPair.setKey(key);
            kVPair.setValue(value);
            kVPair.setAssociationId(associationId);
            kVPair.setAssociationType(associationType);
            return kVPair;
        }
    }
}
