package dev.jianmu.external_parameter.aggregate;

/**
 * @author Daihw
 * @class BaseAssociation
 * @description 抽象关联
 * @create 2022/7/13 2:35 下午
 */
public abstract class BaseAssociation {


    private String associationId;

    private String associationType;

    private String associationPlatform;

    public String getAssociationId() {
        return associationId;
    }

    public String getAssociationType() {
        return associationType;
    }

    public String getAssociationPlatform() {
        return associationPlatform;
    }

    public void updateAssociation(String associationId, String associationType, String associationPlatform) {
        this.associationId = associationId;
        this.associationType = associationType;
        this.associationPlatform = associationPlatform;
        if (associationId == null) {
            this.associationId = "";
        }
        if (associationType == null) {
            this.associationType = "";
        }
        if (associationPlatform == null) {
            this.associationPlatform = "";
        }
    }
}
