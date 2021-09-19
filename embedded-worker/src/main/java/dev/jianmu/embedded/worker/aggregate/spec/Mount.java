package dev.jianmu.embedded.worker.aggregate.spec;

/**
 * @class: Mount
 * @description: Mount
 * @author: Ethan Liu
 * @create: 2021-04-14 10:21
 **/
public class Mount {
    private MountType type;

    private String source;

    private String target;

    private Boolean readOnly;

    public MountType getType() {
        return type;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public static final class Builder {
        private MountType type;
        private String source;
        private String target;
        private Boolean readOnly;

        private Builder() {
        }

        public static Builder aMount() {
            return new Builder();
        }

        public Builder type(MountType type) {
            this.type = type;
            return this;
        }

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public Builder target(String target) {
            this.target = target;
            return this;
        }

        public Builder readOnly(Boolean readOnly) {
            this.readOnly = readOnly;
            return this;
        }

        public Mount build() {
            Mount mount = new Mount();
            mount.readOnly = this.readOnly;
            mount.type = this.type;
            mount.target = this.target;
            mount.source = this.source;
            return mount;
        }
    }
}
