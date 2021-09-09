package dev.jianmu.hub.intergration.aggregate;

/**
 * @class: TaskParameter
 * @description: 任务参数
 * @author: Ethan Liu
 * @create: 2021-04-11 13:42
 **/
public class NodeParameter {
    // 显示名称
    private String name;
    // 唯一引用名称
    private String ref;
    // 参数类型
    private String type;
    // 描述
    private String description;
    // 参数引用Id
    private String parameterId;
    // 参数值
    private String value;

    public String getName() {
        return name;
    }

    public String getRef() {
        return ref;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getParameterId() {
        return parameterId;
    }

    public String getValue() {
        return value;
    }


    public static final class Builder {
        // 显示名称
        private String name;
        // 唯一引用名称
        private String ref;
        // 参数类型
        private String type;
        // 描述
        private String description;
        // 参数引用Id
        private String parameterId;
        // 参数值
        private String value;

        private Builder() {
        }

        public static Builder aNodeParameter() {
            return new Builder();
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder ref(String ref) {
            this.ref = ref;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder parameterId(String parameterId) {
            this.parameterId = parameterId;
            return this;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public NodeParameter build() {
            NodeParameter nodeParameter = new NodeParameter();
            nodeParameter.type = this.type;
            nodeParameter.parameterId = this.parameterId;
            nodeParameter.ref = this.ref;
            nodeParameter.name = this.name;
            nodeParameter.description = this.description;
            nodeParameter.value = this.value;
            return nodeParameter;
        }
    }
}
