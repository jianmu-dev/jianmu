package dev.jianmu.workflow.aggregate.definition;

import dev.jianmu.workflow.aggregate.AggregateRoot;
import dev.jianmu.workflow.aggregate.parameter.Parameter;
import dev.jianmu.workflow.el.*;
import dev.jianmu.workflow.event.definition.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Ethan Liu
 * @program: workflow
 * @description 流程定义实体
 * @create 2021-01-21 14:10
 */
public class Workflow extends AggregateRoot {

    public enum Type {
        WORKFLOW,
        PIPELINE
    }

    // 显示名称
    private String name;
    // 唯一引用名称
    private String ref;
    // 类型
    private Type type;
    // 对应执行器标签
    private String tag;
    private List<String> caches;
    // 描述
    private String description;
    // 版本
    private final String version = UUID.randomUUID().toString().replace("-", "");
    // Node列表
    private Set<Node> nodes;
    // 全局参数
    private Set<GlobalParameter> globalParameters = Set.of();
    // DSL原始内容
    private String dslText;
    // 创建时间
    private final LocalDateTime createdTime = LocalDateTime.now();
    // 表达式计算服务
    private ExpressionLanguage expressionLanguage;
    // 参数上下文
    private EvaluationContext context;

    private Workflow() {
    }

    public void setExpressionLanguage(ExpressionLanguage expressionLanguage) {
        this.expressionLanguage = expressionLanguage;
    }

    public void setContext(EvaluationContext context) {
        this.context = context;
    }

    // 激活节点
    public void activateNode(String triggerId, String nodeRef, int version) {
        Node node = this.findNode(nodeRef);
        if (node instanceof End) {
            // 发布结束节点执行成功事件
            NodeSucceedEvent succeedEvent = NodeSucceedEvent.Builder.aNodeSucceedEvent()
                    .nodeRef(node.getRef())
                    .triggerId(triggerId)
                    .workflowRef(this.ref)
                    .workflowVersion(this.version)
                    .version(version)
                    .build();
            this.raiseEvent(succeedEvent);
            // 发布流程结束事件并返回
            WorkflowEndEvent workflowEndEvent = WorkflowEndEvent.Builder.aWorkflowEndEvent()
                    .nodeRef(node.getRef())
                    .triggerId(triggerId)
                    .workflowRef(this.ref)
                    .workflowVersion(this.version)
                    .build();
            this.raiseEvent(workflowEndEvent);
            return;
        }
        if (node instanceof AsyncTask) {
            AsyncTaskActivatingEvent asyncTaskActivatingEvent = AsyncTaskActivatingEvent.Builder.anAsyncTaskActivatingEvent()
                    .nodeRef(node.getRef())
                    .nodeType(node.getType())
                    .triggerId(triggerId)
                    .workflowRef(this.ref)
                    .workflowVersion(this.version)
                    .version(version)
                    .build();
            this.raiseEvent(asyncTaskActivatingEvent);
            return;
        }
        if (node instanceof Gateway) {
            var branch = ((Gateway) node).calculateTarget(expressionLanguage, context);
            // 发布网关节点执行成功事件
            NodeSucceedEvent succeedEvent = NodeSucceedEvent.Builder.aNodeSucceedEvent()
                    .nodeRef(node.getRef())
                    .triggerId(triggerId)
                    .workflowRef(this.ref)
                    .workflowVersion(this.version)
                    // TODO 3.0需要重新设计
                    .nextTarget(branch.getTarget())
                    .version(version)
                    .build();
            this.raiseEvent(succeedEvent);
        }
    }

    public void next(String triggerId, String nodeRef) {
        Node node = this.findNode(nodeRef);
        if (node instanceof Gateway) {
            var branch = ((Gateway) node).calculateTarget(expressionLanguage, context);
            // 发布下一个节点激活事件
            NodeActivatingEvent activatingEvent = NodeActivatingEvent.Builder.aNodeActivatingEvent()
                    .nodeRef(branch.getTarget())
                    .triggerId(triggerId)
                    .workflowRef(this.ref)
                    .workflowVersion(this.version)
                    .sender(nodeRef)
                    .build();
            this.raiseEvent(activatingEvent);
            // 如果当前激活的是非环回分支，则发布其他节点跳过事件
            if (!branch.isLoop()) {
                // 如果其他分支是环回分支，则不发布跳过事件
                var targets = ((Gateway) node).findNonLoopBranch().stream()
                        // 排除当前命中的分支
                        .filter(targetRef -> !targetRef.equals(branch.getTarget()))
                        .collect(Collectors.toList());
                targets.forEach(targetRef -> {
                    var nodeSkipEvent = NodeSkipEvent.Builder.aNodeSkipEvent()
                            .nodeRef(targetRef)
                            .triggerId(triggerId)
                            .workflowRef(this.ref)
                            .workflowVersion(this.version)
                            .sender(nodeRef)
                            .build();
                    this.raiseEvent(nodeSkipEvent);
                });
            } else {
                // 如果当前激活的是环回分支，则其他环回分支发布跳过事件
                var targets = ((Gateway) node).findLoopBranch().stream()
                        // 排除当前命中的分支
                        .filter(targetRef -> !targetRef.equals(branch.getTarget()))
                        .collect(Collectors.toList());
                targets.forEach(targetRef -> {
                    var nodeSkipEvent = NodeSkipEvent.Builder.aNodeSkipEvent()
                            .nodeRef(targetRef)
                            .triggerId(triggerId)
                            .workflowRef(this.ref)
                            .workflowVersion(this.version)
                            .sender(nodeRef)
                            .build();
                    this.raiseEvent(nodeSkipEvent);
                });
            }
            return;
        }
        // 发布所有下游节点激活事件
        Set<String> nodes = node.getTargets();
        nodes.forEach(n -> {
            NodeActivatingEvent activatingEvent = NodeActivatingEvent.Builder.aNodeActivatingEvent()
                    .nodeRef(n)
                    .triggerId(triggerId)
                    .workflowRef(this.ref)
                    .workflowVersion(this.version)
                    .sender(nodeRef)
                    .build();
            this.raiseEvent(activatingEvent);
        });
    }

    public void start(String triggerId) {
        Node node = this.findStart();
        // 发布开始节点执行成功事件
        NodeSucceedEvent succeedEvent = NodeSucceedEvent.Builder.aNodeSucceedEvent()
                .nodeRef(node.getRef())
                .triggerId(triggerId)
                .workflowRef(this.ref)
                .workflowVersion(this.version)
                .version(0)
                .build();
        this.raiseEvent(succeedEvent);
    }

    // 跳过节点
    public void skipNode(String triggerId, String nodeRef) {
        Node node = this.findNode(nodeRef);
        if (node instanceof End) {
            // 发布流程结束事件并返回
            WorkflowEndEvent workflowEndEvent = WorkflowEndEvent.Builder.aWorkflowEndEvent()
                    .nodeRef(node.getRef())
                    .triggerId(triggerId)
                    .workflowRef(this.ref)
                    .workflowVersion(this.version)
                    .build();
            this.raiseEvent(workflowEndEvent);
            return;
        }
        if (node instanceof Gateway) {
            // 如果存在非环回分支
            if (((Gateway) node).hasNonLoopBranch()) {
                // 过滤环回分支，非环回分支发布跳过事件
                ((Gateway) node).findNonLoopBranch()
                        .forEach(targetRef -> {
                            var nodeSkipEvent = NodeSkipEvent.Builder.aNodeSkipEvent()
                                    .nodeRef(targetRef)
                                    .triggerId(triggerId)
                                    .workflowRef(this.ref)
                                    .workflowVersion(this.version)
                                    .sender(nodeRef)
                                    .build();
                            this.raiseEvent(nodeSkipEvent);
                        });
                return;
            }
        }
        // 发布下游节点跳过事件
        var targets = node.getTargets();
        targets.forEach(targetRef -> {
            var nodeSkipEvent = NodeSkipEvent.Builder.aNodeSkipEvent()
                    .nodeRef(targetRef)
                    .triggerId(triggerId)
                    .workflowRef(this.ref)
                    .workflowVersion(this.version)
                    .sender(nodeRef)
                    .build();
            this.raiseEvent(nodeSkipEvent);
        });
    }

    public Map<String, Parameter<?>> calculateTaskParams(String taskRef) {
        var asyncTask = this.findTask(taskRef);
        return asyncTask.getTaskParameters().stream().map(taskParameter -> {
            var parameter = this.calculateTaskParameter(taskParameter);
            return Map.entry(taskParameter.getRef(), parameter);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Parameter<?> calculateTaskParameter(TaskParameter taskParameter) {
        // 密钥类型单独处理
        if (taskParameter.getType() == Parameter.Type.SECRET) {
            var secret = this.findSecret(taskParameter.getExpression());
            return Parameter.Type.SECRET.newParameter(secret);
        }
        // TODO 适配代码，3.x版本需要删除
        if (taskParameter.getType() == null) {
            var secret = this.findSecret(taskParameter.getExpression());
            if (secret != null) {
                return Parameter.Type.SECRET.newParameter(secret);
            }
        }
        // 计算参数表达式
        var resultType = ResultType.valueOf(taskParameter.getType().name());
        Expression expression = expressionLanguage.parseExpression(taskParameter.getExpression(), resultType);
        EvaluationResult evaluationResult = expressionLanguage.evaluateExpression(expression, context);
        if (evaluationResult.isFailure()) {
            var errorMsg = "参数：" + taskParameter.getRef() +
                    " 表达式: " + taskParameter.getExpression() +
                    " 计算错误: " + evaluationResult.getFailureMessage();
            throw new RuntimeException(errorMsg);
        }
        // TODO 适配代码，3.x版本需要删除
        if (taskParameter.getType() == null) {
            return evaluationResult.getValue();
        }
        // 校验表达式计算结果类型是否与节点定义参数类型匹配
        if (taskParameter.getType() == Parameter.Type.SECRET) {
            return Parameter.Type.SECRET.newParameter(evaluationResult.getValue().getStringValue());
        }
        if (taskParameter.getType() != evaluationResult.getValue().getType()) {
            throw new IllegalArgumentException("表达式:" + taskParameter.getExpression() + " 计算结果类型为" + evaluationResult.getValue().getType() + "与节点定义参数" + taskParameter.getRef() + "类型不匹配");
        }
        return evaluationResult.getValue();
    }

    private String findSecret(String paramValue) {
        Pattern pattern = Pattern.compile("^\\(\\(([a-zA-Z0-9_-]+\\.*[a-zA-Z0-9_-]+)\\)\\)$");
        Matcher matcher = pattern.matcher(paramValue);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private AsyncTask findTask(String taskRef) {
        var node = this.findNode(taskRef);
        if (node instanceof AsyncTask) {
            return (AsyncTask) node;
        }
        throw new RuntimeException("未找到该异步任务：" + taskRef);
    }

    public Node findStart() {
        return this.nodes.stream()
                .filter(n -> n instanceof Start)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未找到启动节点"));
    }

    public Node findNode(String nodeRef) {
        return this.nodes.stream()
                .filter(n -> n.getRef().equals(nodeRef))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未找到该节点定义: " + nodeRef));
    }

    // 返回当前节点上游Node的ref List
    public List<String> findNodes(String nodeRef) {
        Node node = this.findNode(nodeRef);
        return this.nodes.stream()
                .map(Node::getRef)
                .filter(ref -> node.getSources().contains(ref))
                .collect(Collectors.toList());
    }

    // 返回当前节点上游GateWay的ref List
    public List<String> findGateWay(String nodeRef) {
        Node node = this.findNode(nodeRef);
        return this.nodes.stream()
                .filter(n -> n instanceof Gateway)
                .map(Node::getRef)
                .filter(ref -> node.getSources().contains(ref))
                .collect(Collectors.toList());
    }

    // 返回AsyncTask列表
    public List<Node> findTasks() {
        return this.nodes.stream()
                .filter(n -> n instanceof AsyncTask)
                .collect(Collectors.toList());
    }

    // 返回不包含网关节点的当前节点上游Node的ref List
    public List<String> findNodesWithoutGateway(String nodeRef) {
        Node node = this.findNode(nodeRef);
        return this.nodes.stream()
                .filter(n -> !(n instanceof Gateway))
                .map(Node::getRef)
                .filter(ref -> node.getSources().contains(ref))
                .collect(Collectors.toList());
    }

    public String getName() {
        return name;
    }

    public String getRef() {
        return ref;
    }

    public Type getType() {
        return type;
    }

    public String getTag() {
        return this.tag;
    }

    public List<String> getTags() {
        return Arrays.asList(this.tag.split(","));
    }

    public List<String> getCaches() {
        return caches;
    }

    public String getDescription() {
        return description;
    }

    public String getVersion() {
        return version;
    }

    public Set<Node> getNodes() {
        return Set.copyOf(nodes);
    }

    public Set<GlobalParameter> getGlobalParameters() {
        return globalParameters;
    }

    public String getDslText() {
        return dslText;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public static final class Builder {
        // 显示名称
        private String name;
        // 唯一引用名称
        private String ref;
        // 类型
        private Type type;
        // 标签
        private String tag;
        // 缓存
        private List<String> caches;
        // 描述
        private String description;
        // Node列表
        private Set<Node> nodes;
        // 全局参数
        private Set<GlobalParameter> globalParameters;
        // DSL原始内容
        private String dslText;

        private Builder() {
        }

        public static Builder aWorkflow() {
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

        public Builder ref(String ref) {
            this.ref = ref;
            return this;
        }

        public Builder tag(String tag) {
            this.tag = tag;
            return this;
        }

        public Builder caches(List<String> caches) {
            this.caches = caches;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder nodes(Set<Node> nodes) {
            this.nodes = nodes;
            return this;
        }

        public Builder globalParameters(Set<GlobalParameter> globalParameters) {
            this.globalParameters = globalParameters;
            return this;
        }

        public Builder dslText(String dslText) {
            this.dslText = dslText;
            return this;
        }

        public Workflow build() {

            // 添加业务规则检查
            if (this.nodes.size() < 2) {
                throw new RuntimeException("Node数量不能小于2");
            }
            long startCount = this.nodes.stream().filter(node -> node instanceof Start).count();
            if (startCount != 1) {
                throw new RuntimeException("开始节点不存在或多于1个");
            }
            long endCount = this.nodes.stream().filter(node -> node instanceof End).count();
            if (endCount != 1) {
                throw new RuntimeException("结束节点不存在或多于1个");
            }

            boolean d = this.nodes.stream()
                    .collect(Collectors.groupingBy(Node::getRef, Collectors.counting()))
                    .values().stream()
                    .anyMatch(count -> count > 1);
            if (d) {
                throw new RuntimeException("节点唯一引用名称不允许重复");
            }

            this.checkDag();

            Workflow workflow = new Workflow();
            workflow.nodes = Set.copyOf(this.nodes);
            workflow.globalParameters = Set.copyOf(this.globalParameters);
            workflow.ref = this.ref;
            workflow.tag = this.tag;
            workflow.caches = this.caches;
            workflow.dslText = this.dslText;
            workflow.type = this.type;
            workflow.name = this.name;
            workflow.description = this.description;
            return workflow;
        }

        private void checkDag() {
            this.nodes.stream()
                .filter(node -> !(node instanceof Start))
                .filter(node -> !(node instanceof End))
                .forEach(node -> {
                    if (node.getSources().isEmpty()) {
                        throw new RuntimeException("节点\"" + node.getRef() + "\"缺少上游节点");
                    }

                    if (node.getTargets().isEmpty()) {
                        throw new RuntimeException("节点\"" + node.getRef() + "\"缺少下游节点");
                    }
                });
        }
    }
}
