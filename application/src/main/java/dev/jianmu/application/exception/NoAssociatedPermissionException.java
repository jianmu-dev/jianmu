package dev.jianmu.application.exception;

/**
 * @author huangxi
 * @class NoAssociatedPermissionException
 * @description NoAssociatedPermissionException
 * @create 2022-07-20 14:24
 */
public class NoAssociatedPermissionException extends RuntimeException {
    private final String associationId;
    private final String associationType;
    private final String gitRepo;
    private final String gitRepoOwner;

    public NoAssociatedPermissionException(String message, String associationId, String associationType, String gitRepo, String gitRepoOwner) {
        super(message);
        this.associationId = associationId;
        this.associationType = associationType;
        this.gitRepo = gitRepo;
        this.gitRepoOwner = gitRepoOwner;
    }

    public String getGitRepo() {
        return gitRepo;
    }

    public String getGitRepoOwner() {
        return gitRepoOwner;
    }

    public String getAssociationId() {
        return associationId;
    }

    public String getAssociationType() {
        return associationType;
    }
}
