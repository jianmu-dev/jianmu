package dev.jianmu.project.aggregate;

import java.util.UUID;

/**
 * @class GitRepo
 * @description Git仓库模型
 * @author Ethan Liu
 * @create 2021-05-12 16:02
*/
public class GitRepo {

    private String id = UUID.randomUUID().toString().replace("-", "");
    private String uri;
    private Credential credential;
    private String branch;
    private boolean isCloneAllBranches = false;
    private String dslPath;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Credential getCredential() {
        return credential;
    }

    public void setCredential(Credential credential) {
        this.credential = credential;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public boolean isCloneAllBranches() {
        return isCloneAllBranches;
    }

    public void setCloneAllBranches(boolean cloneAllBranches) {
        isCloneAllBranches = cloneAllBranches;
    }

    public String getDslPath() {
        return dslPath;
    }

    public void setDslPath(String dslPath) {
        this.dslPath = dslPath;
    }
}
