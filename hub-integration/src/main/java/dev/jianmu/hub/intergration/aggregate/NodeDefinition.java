package dev.jianmu.hub.intergration.aggregate;

/**
 * @class: NodeDefinition
 * @description: 节点定义
 * @author: Ethan Liu
 * @create: 2021-09-03 14:56
 **/
public class NodeDefinition {
    private String icon;
    private String name;
    private String ownerName;
    private String ownerType;
    private String ownerRef;
    private String creatorName;
    private String creatorRef;
    private String description;
    private String ref;
    private String sourceLink;
    private String documentLink;

    public String getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getOwnerType() {
        return ownerType;
    }

    public String getOwnerRef() {
        return ownerRef;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public String getCreatorRef() {
        return creatorRef;
    }

    public String getDescription() {
        return description;
    }

    public String getRef() {
        return ref;
    }

    public String getSourceLink() {
        return sourceLink;
    }

    public String getDocumentLink() {
        return documentLink;
    }
}
