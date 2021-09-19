package dev.jianmu.task.aggregate;

/**
 * @class: NodeDef
 * @description: 节点定义
 * @author: Ethan Liu
 * @create: 2021-09-19 13:16
 **/
public class NodeInfo {
    private String name;
    private String description;
    private String icon;
    private String ownerName;
    private String ownerType;
    private String ownerRef;
    private String creatorName;
    private String creatorRef;
    private String sourceLink;
    private String documentLink;
    private String type;
    private String workerType;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
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

    public String getSourceLink() {
        return sourceLink;
    }

    public String getDocumentLink() {
        return documentLink;
    }

    public String getType() {
        return type;
    }

    public String getWorkerType() {
        return workerType;
    }

    public static final class Builder {
        private String name;
        private String description;
        private String icon;
        private String ownerName;
        private String ownerType;
        private String ownerRef;
        private String creatorName;
        private String creatorRef;
        private String sourceLink;
        private String documentLink;
        private String type;
        private String workerType;

        private Builder() {
        }

        public static Builder aNodeDef() {
            return new Builder();
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public Builder ownerName(String ownerName) {
            this.ownerName = ownerName;
            return this;
        }

        public Builder ownerType(String ownerType) {
            this.ownerType = ownerType;
            return this;
        }

        public Builder ownerRef(String ownerRef) {
            this.ownerRef = ownerRef;
            return this;
        }

        public Builder creatorName(String creatorName) {
            this.creatorName = creatorName;
            return this;
        }

        public Builder creatorRef(String creatorRef) {
            this.creatorRef = creatorRef;
            return this;
        }

        public Builder sourceLink(String sourceLink) {
            this.sourceLink = sourceLink;
            return this;
        }

        public Builder documentLink(String documentLink) {
            this.documentLink = documentLink;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder workerType(String workerType) {
            this.workerType = workerType;
            return this;
        }

        public NodeInfo build() {
            NodeInfo nodeInfo = new NodeInfo();
            nodeInfo.description = this.description;
            nodeInfo.ownerName = this.ownerName;
            nodeInfo.name = this.name;
            nodeInfo.sourceLink = this.sourceLink;
            nodeInfo.type = this.type;
            nodeInfo.ownerType = this.ownerType;
            nodeInfo.documentLink = this.documentLink;
            nodeInfo.ownerRef = this.ownerRef;
            nodeInfo.workerType = this.workerType;
            nodeInfo.icon = this.icon;
            nodeInfo.creatorName = this.creatorName;
            nodeInfo.creatorRef = this.creatorRef;
            return nodeInfo;
        }
    }
}
