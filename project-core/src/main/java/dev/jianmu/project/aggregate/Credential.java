package dev.jianmu.project.aggregate;

/**
 * @class Credential
 * @description Credential
 * @author Ethan Liu
 * @create 2021-05-15 11:46
*/
public class Credential {
    public enum Type {
        SSH,
        HTTPS
    }

    private Type type;
    private String namespace;
    private String userKey;
    private String passKey;
    private String privateKey;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getPassKey() {
        return passKey;
    }

    public void setPassKey(String passKey) {
        this.passKey = passKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}
