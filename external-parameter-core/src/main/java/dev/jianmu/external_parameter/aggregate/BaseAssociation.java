package dev.jianmu.external_parameter.aggregate;

/**
 * @class BaseAssociation
 * @description 抽象关联
 * @author Daihw
 * @create 2022/7/13 2:35 下午
 */
public abstract class BaseAssociation {


    private String associationId;

    private String associationType;

    public String getAssociationId() {
        return associationId;
    }

    public String getAssociationType() {
        return associationType;
    }

    public void setAssociationId(String associationId) {
        this.associationId = associationId;
    }

    public void setAssociationType(String associationType) {
        this.associationType = associationType;
    }
}
