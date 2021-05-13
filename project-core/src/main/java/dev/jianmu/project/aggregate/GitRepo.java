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

    private String id = UUID.randomUUID().toString().replace("-", "");
    private String uri;
    private Type type;
    private String httpsUsername;
    private String httpsPassword;
    private String PrivateKey;
    private String branch;
    private String directory = UUID.randomUUID().toString().replace("-", "");
    private boolean isCloneAllBranches = false;

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
        return PrivateKey;
    }

    public String getBranch() {
        return branch;
    }

    public String getDirectory() {
        return directory;
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
        PrivateKey = privateKey;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public void setCloneAllBranches(boolean cloneAllBranches) {
        isCloneAllBranches = cloneAllBranches;
    }

    public static final class Builder {
        private String uri;
        private Type type;
        private String httpsUsername;
        private String httpsPassword;
        private String PrivateKey;
        private String branch;
        private boolean isCloneAllBranches = false;

        private Builder() {
        }

        public static Builder aGitRepo() {
            return new Builder();
        }

        public Builder uri(String uri) {
            this.uri = uri;
            return this;
        }

        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        public Builder httpsUsername(String httpsUsername) {
            this.httpsUsername = httpsUsername;
            return this;
        }

        public Builder httpsPassword(String httpsPassword) {
            this.httpsPassword = httpsPassword;
            return this;
        }

        public Builder PrivateKey(String PrivateKey) {
            this.PrivateKey = PrivateKey;
            return this;
        }

        public Builder branch(String branch) {
            this.branch = branch;
            return this;
        }

        public Builder isCloneAllBranches(boolean isCloneAllBranches) {
            this.isCloneAllBranches = isCloneAllBranches;
            return this;
        }

        public GitRepo build() {
            GitRepo gitRepo = new GitRepo();
            gitRepo.PrivateKey = this.PrivateKey;
            gitRepo.uri = this.uri;
            gitRepo.branch = this.branch;
            gitRepo.httpsUsername = this.httpsUsername;
            gitRepo.httpsPassword = this.httpsPassword;
            gitRepo.type = this.type;
            gitRepo.isCloneAllBranches = this.isCloneAllBranches;
            return gitRepo;
        }
    }
}
