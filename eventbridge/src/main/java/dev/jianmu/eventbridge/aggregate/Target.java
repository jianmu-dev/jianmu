package dev.jianmu.eventbridge.aggregate;

import java.util.Set;
import java.util.UUID;

/**
 * @class: Target
 * @description: 事件目标
 * @author: Ethan Liu
 * @create: 2021-08-10 11:40
 **/
public class Target {
    public enum Type {
        TASK,
        WORKFLOW
    }

    private String id;
    private String ref;
    private String bridgeId;
    private String name;
    private Type type;
    private String destinationId;
    private Set<Transformer> transformers;

    public String getId() {
        return id;
    }

    public String getRef() {
        return ref;
    }

    public String getBridgeId() {
        return bridgeId;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public Set<Transformer> getTransformers() {
        return transformers;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setRef(String ref) {
        if (ref != null)
            RefChecker.check(ref);
        this.ref = ref;
    }

    public void setBridgeId(String bridgeId) {
        this.bridgeId = bridgeId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }

    public void setTransformers(Set<Transformer> transformers) {
        this.transformers = transformers;
    }

    public static final class Builder {
        private String ref;
        private String name;
        private String bridgeId;
        private Type type;
        private String destinationId;
        private Set<Transformer> transformers;

        private Builder() {
        }

        public static Builder aTarget() {
            return new Builder();
        }

        public Builder ref(String ref) {
            if (ref != null)
                RefChecker.check(ref);
            this.ref = ref;
            return this;
        }

        public Builder name(String name) {
            if (name == null)
                throw new RuntimeException("name不能为空");
            this.name = name;
            return this;
        }

        public Builder bridgeId(String bridgeId) {
            if (bridgeId == null)
                throw new RuntimeException("bridgeId不能为空");
            this.bridgeId = bridgeId;
            return this;
        }

        public Builder type(Type type) {
            if (type == null)
                throw new RuntimeException("type不能为空");
            this.type = type;
            return this;
        }

        public Builder destinationId(String destinationId) {
            this.destinationId = destinationId;
            return this;
        }

        public Builder transformers(Set<Transformer> transformers) {
            this.transformers = transformers;
            return this;
        }

        public Target build() {
            Target target = new Target();
            target.type = this.type;
            target.id = UUID.randomUUID().toString().replace("-", "");
            target.ref = this.ref;
            target.name = this.name;
            target.bridgeId = this.bridgeId;
            target.destinationId = this.destinationId;
            target.transformers = this.transformers;
            return target;
        }
    }
}
