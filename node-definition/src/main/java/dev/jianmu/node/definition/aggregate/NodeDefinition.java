package dev.jianmu.node.definition.aggregate;

/**
 * @class NodeDefinition
 * @description 节点定义
 * @author Ethan Liu
 * @create 2021-09-03 14:56
*/
public class NodeDefinition {
    public enum Type {
        DOCKER,
        SHELL
    }

    private String id;
    private String icon;
    private String name;
    private String ownerName;
    private String ownerType;
    private String ownerRef;
    private String creatorName;
    private String creatorRef;
    private Type type;
    private String description;
    private String ref;
    private String sourceLink;
    private String documentLink;
    private Boolean deprecated;

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

    public Type getType() {
        return type;
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

    public Boolean getDeprecated() {
        return this.deprecated;
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
        private Type type;
        private String description;
        private String ref;
        private String sourceLink;
        private String documentLink;
        private Boolean deprecated;

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

        public Builder type(Type type) {
            this.type = type;
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

        public Builder deprecated(Boolean deprecated) {
            this.deprecated = deprecated;
            return this;
        }

        public NodeDefinition build() {
            NodeDefinition nodeDefinition = new NodeDefinition();
            nodeDefinition.id = this.id;
            nodeDefinition.name = this.name;
            nodeDefinition.documentLink = this.documentLink;
            nodeDefinition.icon = this.icon;
            nodeDefinition.type = this.type;
            nodeDefinition.ownerType = this.ownerType;
            nodeDefinition.ref = this.ref;
            nodeDefinition.description = this.description;
            nodeDefinition.creatorRef = this.creatorRef;
            nodeDefinition.ownerName = this.ownerName;
            nodeDefinition.ownerRef = this.ownerRef;
            nodeDefinition.sourceLink = this.sourceLink;
            nodeDefinition.creatorName = this.creatorName;
            nodeDefinition.deprecated = this.deprecated;
            return nodeDefinition;
        }
    }
}
