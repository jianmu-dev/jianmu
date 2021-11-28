package dev.jianmu.node.definition.aggregate;

/**
 * @class TaskParameter
 * @description 任务参数
 * @author Ethan Liu
 * @create 2021-04-11 13:42
*/
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
    private Object value;
    // 是否必填
    private Boolean required = false;

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

    public Object getValue() {
        return value;
    }

    public Boolean getRequired() {
        return required;
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
        private Object value;
        // 是否必填
        private Boolean required;

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

        public Builder value(Object value) {
            this.value = value;
            return this;
        }

        public Builder required(Boolean required) {
            this.required = required;
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
            nodeParameter.required = this.required;
            return nodeParameter;
        }
    }
}
