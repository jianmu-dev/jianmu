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

    public NoAssociatedPermissionException(String message, String associationId, String associationType) {
        super(message);
        this.associationId = associationId;
        this.associationType = associationType;
    }

    public String getAssociationId() {
        return associationId;
    }

    public String getAssociationType() {
        return associationType;
    }
}
