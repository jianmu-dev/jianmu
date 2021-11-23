package dev.jianmu.application.dsl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.exception.DslException;
import dev.jianmu.application.query.NodeDef;
import dev.jianmu.node.definition.aggregate.NodeParameter;
import dev.jianmu.node.definition.aggregate.ShellNode;
import dev.jianmu.project.aggregate.Project;
import dev.jianmu.trigger.aggregate.WebhookAuth;
import dev.jianmu.trigger.aggregate.WebhookParameter;
import dev.jianmu.workflow.aggregate.definition.*;
import dev.jianmu.workflow.aggregate.parameter.Parameter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @class DslParser
 * @description DSL解析器
 * @author Ethan Liu
 * @create 2021-09-03 15:01
*/
@Slf4j
public class DslParser {
    private Map<String, Object> trigger;
    private Project.TriggerType triggerType = Project.TriggerType.MANUAL;
    private Webhook webhook;
    private String cron;
    private final Map<String, Map<String, Object>> global = new HashMap<>();
    private Map<String, Object> workflow;
    private Map<String, Object> pipeline;
    private String name;
    private String description;
    private Workflow.Type type;
    private final List<DslNode> dslNodes = new ArrayList<>();
    private final List<ShellNode> shellNodes = new ArrayList<>();
    private Set<GlobalParameter> globalParameters = new HashSet<>();

    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public static DslParser parse(String dslText) {
        var parser = new DslParser();
        try {
            parser = parser.mapper.readValue(dslText, DslParser.class);
            parser.syntaxCheck();
            parser.createGlobalParameters();
        } catch (IOException e) {
            log.error("DSL解析异常:", e);
            throw new DslException("DSL解析异常");
        }
        return parser;
    }

    public Set<Node> createNodes(List<NodeDef> nodeDefs) {
        if (type.equals(Workflow.Type.WORKFLOW)) {
            return this.calculateWorkflowNodes(nodeDefs);
        } else {
            return this.calculatePipelineNodes(nodeDefs);
        }
    }

    public int getSteps() {
        if (this.pipeline != null) {
            return this.dslNodes.size();
        } else {
            return this.dslNodes.size() - 2;
        }
    }

    private Set<Node> calculateWorkflowNodes(List<NodeDef> nodeDefs) {
        // 创建节点
        Map<String, Node> symbolTable = new HashMap<>();
        dslNodes.forEach(dslNode -> {
            if (dslNode.getType().equals("start")) {
                var start = Start.Builder.aStart().name(dslNode.getName()).ref(dslNode.getName()).build();
                symbolTable.put(dslNode.getName(), start);
                return;
            }
            if (dslNode.getType().equals("end")) {
                var end = End.Builder.anEnd().name(dslNode.getName()).ref(dslNode.getName()).build();
                symbolTable.put(dslNode.getName(), end);
                return;
            }
            if (dslNode.getType().equals("condition")) {
                var cases = dslNode.getCases();
                Map<Boolean, String> targetMap = new HashMap<>();
                targetMap.put(true, cases.get("true"));
                targetMap.put(false, cases.get("false"));

                var condition = Condition.Builder.aCondition()
                        .name(dslNode.getName())
                        .ref(dslNode.getName())
                        .expression(dslNode.getExpression())
                        .targetMap(targetMap)
                        .build();
                condition.setTargets(Set.of(cases.get("true"), cases.get("false")));
                symbolTable.put(dslNode.getName(), condition);
                return;
            }
            AsyncTask task;
            if (dslNode.getImage() != null) {
                // 创建Shell Node类型的任务节点
                task = this.createAsyncTask(dslNode);
            } else {
                // 创建任务节点
                var d = nodeDefs.stream()
                        .filter(nodeDef -> dslNode.getType().equals(nodeDef.getType()))
                        .findFirst()
                        .orElseThrow(() -> new DataNotFoundException("未找到任务定义"));
                task = this.createAsyncTask(dslNode, d);
            }
            symbolTable.put(dslNode.getName(), task);
        });
        // 添加节点引用关系
        dslNodes.forEach(dslNode -> {
            var n = symbolTable.get(dslNode.getName());
            if (null != n) {
                dslNode.getTargets().forEach(nodeName -> {
                    var target = symbolTable.get(nodeName);
                    if (null != target) {
                        n.addTarget(target.getRef());
                    } else {
                        throw new DslException("节点" + dslNode.getName() + "指定的target: " + nodeName + "不存在");
                    }
                });
                dslNode.getSources().forEach(nodeName -> {
                    var source = symbolTable.get(nodeName);
                    if (null != source) {
                        n.addSource(source.getRef());
                    } else {
                        throw new DslException("节点" + dslNode.getName() + "指定的source: " + nodeName + "不存在");
                    }
                });
            }
        });
        // 校验并发网关上游下游语法
        symbolTable.values().forEach(node -> {
            node.getTargets().forEach(nodeName -> {
                var target = symbolTable.get(nodeName);
                if (!target.getSources().contains(node.getRef())) {
                    throw new DslException("节点" + nodeName + "未指定source: " + node.getRef());
                }
            });
            node.getSources().forEach(nodeName -> {
                var source = symbolTable.get(nodeName);
                if (!source.getTargets().contains(node.getRef())) {
                    throw new DslException("节点" + nodeName + "未指定target: " + node.getRef());
                }
            });
        });
        return new HashSet<>(symbolTable.values());
    }

    private void createGlobalParameters() {
        var globalParam = this.global.get("param");
        if (globalParam == null) {
            return;
        }
        this.globalParameters = globalParam.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .map(entry -> {
                    String type = null;
                    Object value = null;
                    if (entry.getValue() instanceof String) {
                        value = entry.getValue().toString();
                        type = "STRING";
                    }
                    if (entry.getValue() instanceof Map) {
                        value = ((Map<?, ?>) entry.getValue()).get("value");
                        type = ((Map<String, String>) entry.getValue()).get("type");
                    }
                    return GlobalParameter.Builder.aGlobalParameter()
                            .name(entry.getKey())
                            .type(type)
                            .value(value)
                            .build();
                }).collect(Collectors.toSet());
    }

    private Set<Node> calculatePipelineNodes(List<NodeDef> nodeDefs) {
        // 创建节点
        Map<String, Node> symbolTable = new HashMap<>();
        // 添加开始节点
        var start = Start.Builder.aStart().name("Start").ref("Start").build();
        symbolTable.put("Start", start);
        // 添加结束节点
        var end = End.Builder.anEnd().name("End").ref("End").build();
        symbolTable.put("End", end);
        dslNodes.forEach(dslNode -> {
            AsyncTask task;
            if (dslNode.getImage() != null) {
                // 创建Shell Node类型的任务节点
                task = this.createAsyncTask(dslNode);
            } else {
                // 创建任务节点
                var d = nodeDefs.stream()
                        .filter(nodeDef -> dslNode.getType().equals(nodeDef.getType()))
                        .findFirst()
                        .orElseThrow(() -> new DataNotFoundException("未找到任务定义"));
                task = this.createAsyncTask(dslNode, d);
            }
            symbolTable.put(dslNode.getName(), task);
        });
        // 添加节点引用关系
        dslNodes.forEach(withCounter((i, dslNode) -> {
            var n = symbolTable.get(dslNode.getName());
            if (null != n && i == 0) {
                var startNode = symbolTable.get("Start");
                n.addSource(startNode.getRef());
                startNode.addTarget(n.getRef());
            }
            if (null != n && i == (dslNodes.size() - 1)) {
                var endNode = symbolTable.get("End");
                n.addTarget(endNode.getRef());
                endNode.addSource(n.getRef());
            }
            if (null != n && i > 0) {
                var sourceNode = dslNodes.get(i - 1);
                var source = symbolTable.get(sourceNode.getName());
                n.addSource(source.getRef());
            }
            if (null != n && i < (dslNodes.size() - 1)) {
                var targetNode = dslNodes.get(i + 1);
                var target = symbolTable.get(targetNode.getName());
                n.addTarget(target.getRef());
            }
        }));
        return new HashSet<>(symbolTable.values());
    }

    private AsyncTask createAsyncTask(DslNode dslNode) {
        Set<TaskParameter> taskParameters = Set.of();
        if (dslNode.getEnvironment() != null) {
            taskParameters = AsyncTask.createTaskParameters(dslNode.getEnvironment());
        }
        return AsyncTask.Builder.anAsyncTask()
                .name("Shell Node")
                .ref(dslNode.getName())
                .type(dslNode.getType())
                .taskParameters(taskParameters)
                .description("Shell Node")
                .metadata("{}")
                .build();
    }

    private AsyncTask createAsyncTask(DslNode dslNode, NodeDef nodeDef) {
        this.checkNodeParamRequired(dslNode, nodeDef);
        Set<TaskParameter> taskParameters = Set.of();
        if (dslNode.getParam() != null) {
            taskParameters = AsyncTask.createTaskParameters(dslNode.getParam());
        }
        return AsyncTask.Builder.anAsyncTask()
                .name(nodeDef.getName())
                .ref(dslNode.getName())
                .type(dslNode.getType())
                .taskParameters(taskParameters)
                .description(nodeDef.getDescription())
                .metadata(nodeDef.toJsonString())
                .build();
    }

    private static <T> Consumer<T> withCounter(BiConsumer<Integer, T> consumer) {
        AtomicInteger counter = new AtomicInteger(0);
        return item -> consumer.accept(counter.getAndIncrement(), item);
    }

    /**
     * 校验节点必填参数
     */
    private void checkNodeParamRequired(DslNode dslNode, NodeDef nodeDef) {
        nodeDef.getInputParameters().stream()
                .filter(NodeParameter::getRequired)
                .forEach(param -> {
                    var value = dslNode.getParam().keySet().stream()
                            .filter(k -> param.getRef().equals(k))
                            .findFirst()
                            .orElseThrow(() -> new DslException(dslNode.getName() + "节点的必填参数不能为空"));
                    if (value == null) {
                        throw new DslException(dslNode.getName() + "节点的必填参数不能为空");
                    }
                });
    }

    // DSL语法校验
    private void syntaxCheck() {
        this.triggerSyntaxCheck();
        this.globalParamSyntaxCheck();
        if (null != this.workflow) {
            this.workflowSyntaxCheck();
            this.type = Workflow.Type.WORKFLOW;
            this.workflow.forEach((key, val) -> {
                if (val instanceof Map) {
                    var dslNode = DslNode.of(key, (Map<?, ?>) val);
                    if (dslNode.getImage() != null) {
                        var shellNode = ShellNode.Builder.aShellNode()
                                .image(dslNode.getImage())
                                .environment(dslNode.getEnvironment())
                                .script(dslNode.getScript())
                                .build();
                        dslNode.setType("shell:" + shellNode.getId());
                        shellNodes.add(shellNode);
                    }
                    dslNodes.add(dslNode);
                }
            });
            return;
        }
        if (null != this.pipeline) {
            this.pipelineSyntaxCheck();
            this.type = Workflow.Type.PIPELINE;
            this.pipeline.forEach((key, val) -> {
                if (val instanceof Map) {
                    var dslNode = DslNode.of(key, (Map<?, ?>) val);
                    if (dslNode.getImage() != null) {
                        var shellNode = ShellNode.Builder.aShellNode()
                                .image(dslNode.getImage())
                                .environment(dslNode.getEnvironment())
                                .script(dslNode.getScript())
                                .build();
                        dslNode.setType("shell:" + shellNode.getId());
                        shellNodes.add(shellNode);
                    }
                    dslNodes.add(dslNode);
                }
            });
            return;
        }
        throw new DslException("workflow或pipeline未设置");
    }

    private void triggerSyntaxCheck() {
        if (this.trigger == null) {
            return;
        }
        var triggerType = this.trigger.get("type");
        if (!(triggerType instanceof String)) {
            throw new IllegalArgumentException("trigger type配置错误");
        }
        if (triggerType.equals("cron")) {
            var schedule = this.trigger.get("schedule");
            if (!(schedule instanceof String)) {
                throw new IllegalArgumentException("schedule未配置");
            }
            this.triggerType = Project.TriggerType.CRON;
            this.cron = (String) schedule;
        }
        if (triggerType.equals("webhook")) {
            var param = this.trigger.get("param");
            var auth = this.trigger.get("auth");
            var matcher = this.trigger.get("matcher");
            var webhookBuilder = Webhook.builder();
            if (matcher instanceof String) {
                if (!this.isEl((String) matcher)) {
                    throw new IllegalArgumentException("matcher表达式格式错误");
                }
                webhookBuilder.matcher((String) matcher);
            }
            if (auth instanceof Map) {
                var token = ((Map<?, ?>) auth).get("token");
                var value = ((Map<?, ?>) auth).get("value");
                if (token instanceof String && value instanceof String) {
                    webhookBuilder.auth(
                            WebhookAuth.Builder.aWebhookAuth()
                                    .token((String) token)
                                    .value((String) value)
                                    .build()
                    );
                }
            }
            if (param instanceof List) {
                var ps = ((List<?>) param).stream()
                        .filter(p -> p instanceof Map)
                        .map(p -> (Map<String, Object>) p)
                        .map(p -> {
                            var name = p.get("name");
                            var type = p.get("type");
                            var exp = p.get("exp");
                            if (!(name instanceof String)) {
                                throw new IllegalArgumentException("Webhook参数名配置错误");
                            }
                            if (!(type instanceof String)) {
                                throw new IllegalArgumentException("Webhook参数类型配置错误");
                            }
                            if (!(exp instanceof String)) {
                                throw new IllegalArgumentException("Webhook参数表达式配置错误");
                            }
                            Parameter.Type.getTypeByName((String) type);
                            return WebhookParameter.Builder.aWebhookParameter()
                                    .name((String) name)
                                    .type((String) type)
                                    .exp((String) exp)
                                    .build();
                        }).collect(Collectors.toList());
                webhookBuilder.param(ps);
            }
            this.webhook = webhookBuilder.build();
            this.triggerType = Project.TriggerType.WEBHOOK;
        }
    }

    private void globalParamSyntaxCheck() {
        var globalParam = this.global.get("param");
        if (globalParam == null) {
            return;
        }
        globalParam.forEach((k, v) -> {
            if (v instanceof Map) {
                var type = ((Map<String, String>) v).get("type");
                var value = ((Map<?, ?>) v).get("value");
                var p = Parameter.Type.getTypeByName(type).newParameter(value);
                if (Parameter.Type.SECRET == p.getType()) {
                    throw new DslException("全局参数不支持使用SECRET类型");
                }
            }
        });
    }

    private void pipelineSyntaxCheck() {
        var pipe = this.pipeline;
        if (null == pipe.get("name")) {
            throw new DslException("pipeline name未设置");
        }
        this.name = (String) pipe.get("name");
        this.description = (String) pipe.get("description");
        pipe.forEach((key, val) -> {
            if (val instanceof Map) {
                this.checkPipeNode(key, (Map<?, ?>) val);
            }
        });
    }

    private void checkPipeNode(String nodeName, Map<?, ?> node) {
        // 验证保留关键字
        if (nodeName.equals("event")) {
            throw new DslException("节点名称不能使用event");
        }
        if (nodeName.equals("global")) {
            throw new DslException("节点名称不能使用global");
        }
        // 如果为Shell Node，不校验type
        var image = node.get("image");
        if (image != null) {
            return;
        }
        var type = node.get("type");
        if (null == type) {
            throw new DslException("Node type未设置");
        }
    }

    private void workflowSyntaxCheck() {
        var flow = this.workflow;
        if (null == flow.get("name")) {
            throw new DslException("workflow name未设置");
        }
        this.name = (String) flow.get("name");
        this.description = (String) flow.get("description");
        flow.forEach((key, val) -> {
            if (val instanceof Map) {
                this.checkNode(key, (Map<?, ?>) val);
            }
        });
    }

    private void checkNode(String nodeName, Map<?, ?> node) {
        // 如果为Shell Node，不校验type
        var image = node.get("image");
        if (image != null) {
            return;
        }
        var type = node.get("type");
        if (null == type) {
            throw new DslException("Node type未设置");
        }
        if (type.equals("start")) {
            this.checkStart(node);
            return;
        }
        if (type.equals("end")) {
            this.checkEnd(node);
            return;
        }
        if (type.equals("condition")) {
            this.checkCondition(node);
            return;
        }
        // 验证保留关键字
        if (nodeName.equals("event")) {
            throw new DslException("节点名称不能使用event");
        }
        if (nodeName.equals("global")) {
            throw new DslException("节点名称不能使用global");
        }
        this.checkTask(nodeName, node);
    }

    private void checkTask(String nodeName, Map<?, ?> node) {
        var sources = node.get("sources");
        var targets = node.get("targets");
        if (!(sources instanceof List) || ((List<?>) sources).isEmpty()) {
            throw new DslException("任务节点" + nodeName + "sources未设置");
        }
        if (!(targets instanceof List) || ((List<?>) targets).isEmpty()) {
            throw new DslException("任务节点" + nodeName + "targets未设置");
        }
    }

    private void checkCondition(Map<?, ?> node) {
        var expression = node.get("expression");
        var cases = node.get("cases");
        if (null == expression) {
            throw new DslException("条件网关expression未设置");
        }
        if (!(cases instanceof Map<?, ?>)) {
            throw new DslException("条件网关cases未设置");
        }
        if (((Map<?, ?>) cases).size() != 2) {
            throw new DslException("cases数量错误");
        }
    }

    private void checkEnd(Map<?, ?> node) {
        var sources = node.get("sources");
        if (!(sources instanceof List)) {
            throw new DslException("结束节点sources未设置");
        }
    }

    private void checkStart(Map<?, ?> node) {
        var targets = node.get("targets");
        if (!(targets instanceof List)) {
            throw new DslException("开始节点targets未设置");
        }
    }

    private boolean isEl(String paramValue) {
        Pattern pattern = Pattern.compile("^\\(.*\\)$");
        Matcher matcher = pattern.matcher(paramValue);
        return matcher.lookingAt();
    }

    public Set<String> getAsyncTaskTypes() {
        return dslNodes.stream()
                .map(DslNode::getType)
                .filter(type -> !type.equals("start"))
                .filter(type -> !type.equals("end"))
                .filter(type -> !type.equals("condition"))
                .filter(type -> !type.startsWith("shell:"))
                .map(type -> {
                    String[] strings = type.split(":");
                    if (strings.length == 0) {
                        return type + ":latest";
                    }
                    return type;
                })
                .collect(Collectors.toSet());
    }

    public Project.TriggerType getTriggerType() {
        return triggerType;
    }

    public String getCron() {
        return cron;
    }

    public Map<String, Object> getTrigger() {
        return trigger;
    }

    public Webhook getWebhook() {
        return webhook;
    }

    public Map<String, Map<String, Object>> getGlobal() {
        return global;
    }

    public Map<String, Object> getWorkflow() {
        return workflow;
    }

    public Map<String, Object> getPipeline() {
        return pipeline;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Workflow.Type getType() {
        return type;
    }

    public List<DslNode> getDslNodes() {
        return dslNodes;
    }

    public List<ShellNode> getShellNodes() {
        return shellNodes;
    }

    public Set<GlobalParameter> getGlobalParameters() {
        return globalParameters;
    }
}
