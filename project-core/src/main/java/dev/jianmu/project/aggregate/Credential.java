package dev.jianmu.project.aggregate;

/**
 * @class: Credential
 * @description: Credential
 * @author: Ethan Liu
 * @create: 2021-05-15 11:46
 **/
public abstract class Credential {
    private String namespace;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
