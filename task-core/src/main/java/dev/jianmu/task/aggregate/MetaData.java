package dev.jianmu.task.aggregate;

/**
 * @class: MetaData
 * @description: MetaData
 * @author: Ethan Liu
 * @create: 2021-05-29 08:42
 **/
public class MetaData {
    private String name;
    private String group;
    private String icon;
    private String description;
    private String tags;
    private String docs;
    private String owner;
    private String source;

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    public String getTags() {
        return tags;
    }

    public String getDocs() {
        return docs;
    }

    public String getOwner() {
        return owner;
    }

    public String getSource() {
        return source;
    }

    public static final class Builder {
        private String name;
        private String group;
        private String icon;
        private String description;
        private String tags;
        private String docs;
        private String owner;
        private String source;

        private Builder() {
        }

        public static Builder aMetaData() {
            return new Builder();
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder group(String group) {
            this.group = group;
            return this;
        }

        public Builder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder tags(String tags) {
            this.tags = tags;
            return this;
        }

        public Builder docs(String docs) {
            this.docs = docs;
            return this;
        }

        public Builder owner(String owner) {
            this.owner = owner;
            return this;
        }

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public MetaData build() {
            MetaData metaData = new MetaData();
            metaData.name = this.name;
            metaData.owner = this.owner;
            metaData.tags = this.tags;
            metaData.icon = this.icon;
            metaData.source = this.source;
            metaData.group = this.group;
            metaData.description = this.description;
            metaData.docs = this.docs;
            return metaData;
        }
    }
}
