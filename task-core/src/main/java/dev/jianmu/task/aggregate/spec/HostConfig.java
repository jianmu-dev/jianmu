package dev.jianmu.task.aggregate.spec;

import java.util.List;

/**
 * @class: HostConfig
 * @description: TODO
 * @author: Ethan Liu
 * @create: 2021-04-14 10:20
 **/
public class HostConfig {
    private List<Mount> mounts;

    public List<Mount> getMounts() {
        return mounts;
    }

    public static final class Builder {
        private List<Mount> mounts;

        private Builder() {
        }

        public static Builder aHostConfig() {
            return new Builder();
        }

        public Builder mounts(List<Mount> mounts) {
            this.mounts = mounts;
            return this;
        }

        public HostConfig build() {
            HostConfig hostConfig = new HostConfig();
            hostConfig.mounts = this.mounts;
            return hostConfig;
        }
    }
}
