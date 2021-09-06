package dev.jianmu.hub.intergration.aggregate;

/**
 * @class: NodeDef
 * @description: 节点定义
 * @author: Ethan Liu
 * @create: 2021-09-04 11:59
 **/
public class NodeDef {
    private String name;
    private String description;
    private String type;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }


    public static final class Builder {
        private String name;
        private String description;
        private String type;

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

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public NodeDef build() {
            NodeDef nodeDef = new NodeDef();
            nodeDef.name = this.name;
            nodeDef.type = this.type;
            nodeDef.description = this.description;
            return nodeDef;
        }
    }
}
