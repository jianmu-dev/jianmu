package dev.jianmu.project.aggregate;

import java.util.UUID;

/**
 * @class: GitRepo
 * @description: Git仓库模型
 * @author: Ethan Liu
 * @create: 2021-05-12 16:02
 **/
public class GitRepo {
    public enum Type {
        HTTPS,
        SSH
    }

    private final String id = UUID.randomUUID().toString().replace("-", "");
    private String uri;
    private Type type;
    private String httpsUsername;
    private String httpsPassword;
    private String privateKey;
    private String branch;
    private boolean isCloneAllBranches = false;
    private String dslPath;

    public String getDslPath() {
        return dslPath;
    }

    public void setDslPath(String dslPath) {
        this.dslPath = dslPath;
    }

    public String getId() {
        return id;
    }

    public String getUri() {
        return uri;
    }

    public Type getType() {
        return type;
    }

    public String getHttpsUsername() {
        return httpsUsername;
    }

    public String getHttpsPassword() {
        return httpsPassword;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getBranch() {
        return branch;
    }

    public boolean isCloneAllBranches() {
        return isCloneAllBranches;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setHttpsUsername(String httpsUsername) {
        this.httpsUsername = httpsUsername;
    }

    public void setHttpsPassword(String httpsPassword) {
        this.httpsPassword = httpsPassword;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public void setCloneAllBranches(boolean cloneAllBranches) {
        isCloneAllBranches = cloneAllBranches;
    }
}
