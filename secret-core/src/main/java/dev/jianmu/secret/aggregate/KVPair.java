package dev.jianmu.secret.aggregate;

/**
 * @class: KVPair
 * @description: 键值对
 * @author: Ethan Liu
 * @create: 2021-04-20 12:40
 **/
public class KVPair {
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

        public KVPair build() {
            KVPair kVPair = new KVPair();
            kVPair.setNamespaceName(namespaceName);
            kVPair.setKey(key);
            kVPair.setValue(value);
            return kVPair;
        }
    }
}
