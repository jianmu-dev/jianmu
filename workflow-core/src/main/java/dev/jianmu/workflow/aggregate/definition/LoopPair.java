package dev.jianmu.workflow.aggregate.definition;

/**
 * @author Ethan Liu
 * @class LoopPair
 * @description 环路对 - 当前节点在某一环路中时，该环路对应的上游与下游节点
 * @create 2022-03-20 16:23
 */
public class LoopPair {
    private String source;
    private String target;

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public static final class Builder {
        private String source;
        private String target;

        private Builder() {
        }

        public static Builder aLoopPair() {
            return new Builder();
        }

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public Builder target(String target) {
            this.target = target;
            return this;
        }

        public LoopPair build() {
            LoopPair loopPair = new LoopPair();
            loopPair.source = this.source;
            loopPair.target = this.target;
            return loopPair;
        }
    }
}
