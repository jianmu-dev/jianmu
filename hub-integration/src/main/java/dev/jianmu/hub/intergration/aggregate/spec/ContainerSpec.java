package dev.jianmu.hub.intergration.aggregate.spec;

import java.util.List;
import java.util.Map;

/**
 * @class: ContainerSpec
 * @description: 容器规格定义
 * @author: Ethan Liu
 * @create: 2021-04-14 20:30
 **/
public class ContainerSpec {
    private String name;
    private String hostName;
    private String domainName;
    private String user;
    private String[] env;
    private String[] cmd;
    private String[] entrypoint;
    private String image;
    private String workingDir;
    private List<String> onBuild;
    private Boolean networkDisabled;
    private String stopSignal;
    private Integer stopTimeout;
    private HostConfig hostConfig = new HostConfig();
    private Map<String, String> labels;
    private List<String> shell;

    public String getName() {
        return name;
    }

    public String getHostName() {
        return hostName;
    }

    public String getDomainName() {
        return domainName;
    }

    public String getUser() {
        return user;
    }

    public String[] getEnv() {
        return env;
    }

    public String[] getCmd() {
        return cmd;
    }

    public String[] getEntrypoint() {
        return entrypoint;
    }

    public String getImage() {
        return image;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public List<String> getOnBuild() {
        return onBuild;
    }

    public Boolean getNetworkDisabled() {
        return networkDisabled;
    }

    public String getStopSignal() {
        return stopSignal;
    }

    public Integer getStopTimeout() {
        return stopTimeout;
    }

    public HostConfig getHostConfig() {
        return hostConfig;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public List<String> getShell() {
        return shell;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String hostName;
        private String domainName;
        private String user;
        private String[] env;
        private String[] cmd;
        private String[] entrypoint;
        private String image;
        private String workingDir;
        private List<String> onBuild;
        private Boolean networkDisabled;
        private String stopSignal;
        private Integer stopTimeout;
        private HostConfig hostConfig = new HostConfig();
        private Map<String, String> labels;
        private List<String> shell;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder hostName(String hostName) {
            this.hostName = hostName;
            return this;
        }

        public Builder domainName(String domainName) {
            this.domainName = domainName;
            return this;
        }

        public Builder user(String user) {
            this.user = user;
            return this;
        }

        public Builder env(String[] env) {
            this.env = env;
            return this;
        }

        public Builder cmd(String[] cmd) {
            this.cmd = cmd;
            return this;
        }

        public Builder entrypoint(String[] entrypoint) {
            this.entrypoint = entrypoint;
            return this;
        }

        public Builder image(String image) {
            this.image = image;
            return this;
        }

        public Builder workingDir(String workingDir) {
            this.workingDir = workingDir;
            return this;
        }

        public Builder onBuild(List<String> onBuild) {
            this.onBuild = onBuild;
            return this;
        }

        public Builder networkDisabled(Boolean networkDisabled) {
            this.networkDisabled = networkDisabled;
            return this;
        }

        public Builder stopSignal(String stopSignal) {
            this.stopSignal = stopSignal;
            return this;
        }

        public Builder stopTimeout(Integer stopTimeout) {
            this.stopTimeout = stopTimeout;
            return this;
        }

        public Builder hostConfig(HostConfig hostConfig) {
            this.hostConfig = hostConfig;
            return this;
        }

        public Builder labels(Map<String, String> labels) {
            this.labels = labels;
            return this;
        }

        public Builder shell(List<String> shell) {
            this.shell = shell;
            return this;
        }

        public ContainerSpec build() {
            ContainerSpec containerSpec = new ContainerSpec();
            containerSpec.onBuild = this.onBuild;
            containerSpec.user = this.user;
            containerSpec.shell = this.shell;
            containerSpec.stopTimeout = this.stopTimeout;
            containerSpec.labels = this.labels;
            containerSpec.name = this.name;
            containerSpec.image = this.image;
            containerSpec.domainName = this.domainName;
            containerSpec.cmd = this.cmd;
            containerSpec.hostConfig = this.hostConfig;
            containerSpec.env = this.env;
            containerSpec.entrypoint = this.entrypoint;
            containerSpec.hostName = this.hostName;
            containerSpec.workingDir = this.workingDir;
            containerSpec.networkDisabled = this.networkDisabled;
            containerSpec.stopSignal = this.stopSignal;
            return containerSpec;
        }
    }
}
