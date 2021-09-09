package dev.jianmu.hub.intergration.aggregate;

/**
 * @class: NodeDefinition
 * @description: 节点定义
 * @author: Ethan Liu
 * @create: 2021-09-03 14:56
 **/
public class NodeDefinition {
    private String id;
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

    public String getId() {
        return id;
    }

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

    public static final class Builder {
        private String id;
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

        private Builder() {
        }

        public static Builder aNodeDefinition() {
            return new Builder();
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
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

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder ref(String ref) {
            this.ref = ref;
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

        public NodeDefinition build() {
            NodeDefinition nodeDefinition = new NodeDefinition();
            nodeDefinition.description = this.description;
            nodeDefinition.id = this.id;
            nodeDefinition.ownerRef = this.ownerRef;
            nodeDefinition.creatorName = this.creatorName;
            nodeDefinition.name = this.name;
            nodeDefinition.documentLink = this.documentLink;
            nodeDefinition.creatorRef = this.creatorRef;
            nodeDefinition.ref = this.ref;
            nodeDefinition.sourceLink = this.sourceLink;
            nodeDefinition.ownerType = this.ownerType;
            nodeDefinition.icon = this.icon;
            nodeDefinition.ownerName = this.ownerName;
            return nodeDefinition;
        }
    }
}
