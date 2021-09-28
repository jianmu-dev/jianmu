package dev.jianmu.application.dsl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.jianmu.application.query.NodeDef;
import dev.jianmu.workflow.aggregate.definition.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @class: DslParser
 * @description: DSL解析器
 * @author: Ethan Liu
 * @create: 2021-09-03 15:01
 **/
@Slf4j
public class DslParser {
    private String cron;
    private String eb;
    private Map<String, String> param;
    private Map<String, Object> workflow;
    private Map<String, Object> pipeline;
    private String name;
    private String ref;
    private String description;
    private Workflow.Type type;
    private final List<DslNode> dslNodes = new ArrayList<>();

    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public static DslParser parse(String dslText) {
        var parser = new DslParser();
        try {
            parser = parser.mapper.readValue(dslText, DslParser.class);
            parser.syntaxCheck();
        } catch (IOException e) {
            log.error("DSL解析异常:", e);
            throw new RuntimeException("DSL解析异常");
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
        return this.dslNodes.size() - 2;
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
            // 创建任务节点
            var n = nodeDefs.stream()
                    .filter(nodeDef -> dslNode.getType().equals(nodeDef.getType()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("未找到任务定义"));
            var task = this.createAsyncTask(dslNode, n);
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
                    }
                });
                dslNode.getSources().forEach(nodeName -> {
                    var source = symbolTable.get(nodeName);
                    if (null != source) {
                        n.addSource(source.getRef());
                    }
                });
            }
        });
        return new HashSet<>(symbolTable.values());
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
            // 创建任务节点
            var d = nodeDefs.stream()
                    .filter(nodeDef -> dslNode.getType().equals(nodeDef.getType()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("未找到任务定义"));
            var task = this.createAsyncTask(dslNode, d);
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

    private AsyncTask createAsyncTask(DslNode dslNode, NodeDef nodeDef) {
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
                .build();
    }

    private static <T> Consumer<T> withCounter(BiConsumer<Integer, T> consumer) {
        AtomicInteger counter = new AtomicInteger(0);
        return item -> consumer.accept(counter.getAndIncrement(), item);
    }

    // DSL语法校验
    private void syntaxCheck() {
        if (null != this.workflow) {
            this.workflowSyntaxCheck();
            this.type = Workflow.Type.WORKFLOW;
            this.workflow.forEach((key, val) -> {
                if (val instanceof Map) {
                    dslNodes.add(new DslNode(key, (Map<?, ?>) val));
                }
            });
            return;
        }
        if (null != this.pipeline) {
            this.pipelineSyntaxCheck();
            this.type = Workflow.Type.PIPELINE;
            this.pipeline.forEach((key, val) -> {
                if (val instanceof Map) {
                    dslNodes.add(new DslNode(key, (Map<?, ?>) val));
                }
            });
            return;
        }
        throw new RuntimeException("workflow或pipeline未设置");
    }

    private void pipelineSyntaxCheck() {
        var pipe = this.pipeline;
        if (null == pipe.get("name")) {
            throw new RuntimeException("pipeline name未设置");
        }
        if (null == pipe.get("ref")) {
            throw new RuntimeException("pipeline ref未设置");
        }
        this.name = (String) pipe.get("name");
        this.ref = (String) pipe.get("ref");
        this.description = (String) pipe.get("description");
        pipe.forEach((key, val) -> {
            if (val instanceof Map) {
                this.checkPipeNode(key, (Map<?, ?>) val);
            }
        });
    }

    private void checkPipeNode(String nodeName, Map<?, ?> node) {
        var type = node.get("type");
        if (null == type) {
            throw new RuntimeException("Node type未设置");
        }
        // 验证保留关键字
        if (nodeName.equals("event")) {
            throw new RuntimeException("节点名称不能使用event");
        }
        if (nodeName.equals("global")) {
            throw new RuntimeException("节点名称不能使用global");
        }
    }

    private void workflowSyntaxCheck() {
        var flow = this.workflow;
        if (null == flow.get("name")) {
            throw new RuntimeException("workflow name未设置");
        }
        if (null == flow.get("ref")) {
            throw new RuntimeException("workflow ref未设置");
        }
        this.name = (String) flow.get("name");
        this.ref = (String) flow.get("ref");
        this.description = (String) flow.get("description");
        flow.forEach((key, val) -> {
            if (val instanceof Map) {
                this.checkNode(key, (Map<?, ?>) val);
            }
        });
    }

    private void checkNode(String nodeName, Map<?, ?> node) {
        var type = node.get("type");
        if (null == type) {
            throw new RuntimeException("Node type未设置");
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
            throw new RuntimeException("节点名称不能使用event");
        }
        if (nodeName.equals("global")) {
            throw new RuntimeException("节点名称不能使用global");
        }
        this.checkTask(nodeName, node);
    }

    private void checkTask(String nodeName, Map<?, ?> node) {
        var sources = node.get("sources");
        var targets = node.get("targets");
        if (!(sources instanceof List) || ((List<?>) sources).isEmpty()) {
            throw new RuntimeException("任务节点" + nodeName + "sources未设置");
        }
        if (!(targets instanceof List) || ((List<?>) targets).isEmpty()) {
            throw new RuntimeException("任务节点" + nodeName + "targets未设置");
        }
    }

    private void checkCondition(Map<?, ?> node) {
        var expression = node.get("expression");
        var cases = node.get("cases");
        if (null == expression) {
            throw new RuntimeException("条件网关expression未设置");
        }
        if (!(cases instanceof Map<?, ?>)) {
            throw new RuntimeException("条件网关cases未设置");
        }
        if (((Map<?, ?>) cases).size() != 2) {
            throw new RuntimeException("cases数量错误");
        }
    }

    private void checkEnd(Map<?, ?> node) {
        var sources = node.get("sources");
        if (!(sources instanceof List)) {
            throw new RuntimeException("结束节点sources未设置");
        }
    }

    private void checkStart(Map<?, ?> node) {
        var targets = node.get("targets");
        if (!(targets instanceof List)) {
            throw new RuntimeException("开始节点targets未设置");
        }
    }

    public Set<String> getAsyncTaskTypes() {
        return dslNodes.stream()
                .map(DslNode::getType)
                .filter(type -> !type.equals("start"))
                .filter(type -> !type.equals("end"))
                .filter(type -> !type.equals("condition"))
                .map(type -> {
                    String[] strings = type.split(":");
                    if (strings.length == 0) {
                        return type + ":latest";
                    }
                    return type;
                })
                .collect(Collectors.toSet());
    }

    public String getCron() {
        return cron;
    }

    public String getEb() {
        return eb;
    }

    public Map<String, String> getParam() {
        return param;
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

    public String getRef() {
        return ref;
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
}
