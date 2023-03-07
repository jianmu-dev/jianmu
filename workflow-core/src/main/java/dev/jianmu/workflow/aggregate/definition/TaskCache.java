package dev.jianmu.workflow.aggregate.definition;

/**
 * @class TaskCache
 * @description 任务缓存
 * @author Daihw
 * @create 2023/3/6 3:49 下午
 */
public class TaskCache {
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

        public static Builder aTaskCache() {
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

        public TaskCache build() {
            TaskCache taskCache = new TaskCache();
            taskCache.source = this.source;
            taskCache.target = this.target;
            return taskCache;
        }
    }
}
