package dev.jianmu.node.definition.aggregate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @class ShellNode
 * @description Shell节点定义
 * @author Ethan Liu
 * @create 2021-11-08 13:24
 */
public class ShellNode {
    // Id
    private String id;
    // 镜像名称
    private String image;
    // 环境变量
    private Map<String, String> environment;
    // 命令列表
    private List<String> script;

    public String getId() {
        return id;
    }

    public String getImage() {
        return image;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public List<String> getScript() {
        return script;
    }

    public static final class Builder {
        // 镜像名称
        private String image;
        // 环境变量
        private Map<String, String> environment;
        // 命令列表
        private List<String> script;

        private Builder() {
        }

        public static Builder aShellNode() {
            return new Builder();
        }

        public Builder image(String image) {
            this.image = image;
            return this;
        }

        public Builder environment(Map<String, String> environment) {
            this.environment = environment;
            return this;
        }

        public Builder script(List<String> script) {
            this.script = script;
            return this;
        }

        public ShellNode build() {
            ShellNode shellNode = new ShellNode();
            shellNode.id = UUID.randomUUID().toString().replace("-", "");
            shellNode.image = this.image;
            shellNode.environment = this.environment;
            shellNode.script = this.script;
            return shellNode;
        }
    }
}
