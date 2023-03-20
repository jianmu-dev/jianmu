package dev.jianmu.workflow.aggregate.definition;

import dev.jianmu.workflow.aggregate.parameter.Parameter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Ethan Liu
 * @program: workflow
 * @description 异步任务节点
 * @create 2021-01-21 20:42
 */
public class AsyncTask extends BaseNode {
    private AsyncTask() {
    }

    public static Set<TaskParameter> createTaskParameters(Map<String, String> param, Map<String, String> inputParameters) {
        return param.entrySet().stream().map(entry -> {
                    var typeString = inputParameters.get(entry.getKey());
                    if (typeString == null) {
                        throw new IllegalArgumentException("节点定义中未找到输入参数名为:" + entry.getKey() + "的参数定义");
                    }
                    var type = Parameter.Type.getTypeByName(typeString);
                    return TaskParameter.Builder.aTaskParameter()
                            .ref(entry.getKey())
                            .type(type)
                            .expression(entry.getValue())
                            .build();
                }
        ).collect(Collectors.toSet());
    }

    public static Set<TaskParameter> createTaskParameters(Map<String, String> param) {
        return param.entrySet().stream().map(entry ->
                TaskParameter.Builder.aTaskParameter()
                        .ref(entry.getKey())
                        .type(findEnvironmentType(entry.getValue()))
                        .expression(entry.getValue())
                        .build()
        ).collect(Collectors.toSet());
    }

    private static Parameter.Type findEnvironmentType(String paramValue) {
        Pattern pattern = Pattern.compile("^\\(\\(([a-zA-Z0-9_-]+\\.*[a-zA-Z0-9_-]+)\\)\\)$");
        Matcher matcher = pattern.matcher(paramValue);
        if (matcher.find()) {
            return Parameter.Type.SECRET;
        }
        return Parameter.Type.STRING;
    }

    public static List<TaskCache> createCaches(Map<String, String> cache) {
        if (cache == null) {
            return null;
        }
        return cache.entrySet().stream().map(entry ->
                TaskCache.Builder.aTaskCache()
                        .source(entry.getKey())
                        .target(entry.getValue())
                        .build()
        ).collect(Collectors.toList());
    }

    public static final class Builder {
        // 显示名称
        protected String name;
        // 唯一引用名称
        protected String ref;
        // 描述
        protected String description;
        // 类型
        protected String type;
        // 节点元数据快照
        protected String metadata;
        private Set<TaskParameter> taskParameters;
        private List<TaskCache> taskCaches;

        private Builder() {
        }

        public static Builder anAsyncTask() {
            return new Builder();
        }

        public Builder taskParameters(Set<TaskParameter> taskParameters) {
            this.taskParameters = taskParameters;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder ref(String ref) {
            this.ref = ref;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder metadata(String metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder taskCaches(List<TaskCache> taskCaches) {
            this.taskCaches = taskCaches;
            return this;
        }

        public AsyncTask build() {
            AsyncTask asyncTask = new AsyncTask();
            asyncTask.name = this.name;
            asyncTask.ref = this.ref;
            asyncTask.description = this.description;
            asyncTask.taskParameters = this.taskParameters;
            asyncTask.type = this.type;
            asyncTask.metadata = this.metadata;
            asyncTask.taskCaches = this.taskCaches;
            return asyncTask;
        }
    }
}
