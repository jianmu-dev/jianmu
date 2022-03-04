package dev.jianmu.workflow.aggregate.definition;

/**
 * @author Ethan Liu
 * @class Branch
 * @description 条件分支
 * @create 2022-03-01 15:54
 */
public class Branch {
    private Object matchedCondition;
    private String target;
    private boolean loop;

    private Branch() {
    }

    public Object getMatchedCondition() {
        return matchedCondition;
    }

    public String getTarget() {
        return target;
    }

    public boolean isLoop() {
        return loop;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public static final class Builder {
        private Object matchedCondition;
        private String target;
        private boolean loop = false;

        private Builder() {
        }

        public static Builder aBranch() {
            return new Builder();
        }

        public Builder matchedCondition(Object matchedCondition) {
            this.matchedCondition = matchedCondition;
            return this;
        }

        public Builder target(String target) {
            this.target = target;
            return this;
        }

        public Builder loop(boolean loop) {
            this.loop = loop;
            return this;
        }

        public Branch build() {
            Branch branch = new Branch();
            branch.target = this.target;
            branch.loop = this.loop;
            branch.matchedCondition = this.matchedCondition;
            return branch;
        }
    }
}
