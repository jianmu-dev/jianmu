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
    private String name;
    private Type type;
    private String destinationId;
    private Set<Transformer> transformers;

    public String getId() {
        return id;
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

    public void setTransformers(Set<Transformer> transformers) {
        this.transformers = transformers;
    }

    public static final class Builder {
        private String name;
        private Type type;
        private String destinationId;
        private Set<Transformer> transformers;

        private Builder() {
        }

        public static Builder aTarget() {
            return new Builder();
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(Type type) {
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
            target.id = UUID.randomUUID().toString().replace("-", "");
            target.type = this.type;
            target.name = this.name;
            target.destinationId = this.destinationId;
            target.transformers = this.transformers;
            return target;
        }
    }
}
