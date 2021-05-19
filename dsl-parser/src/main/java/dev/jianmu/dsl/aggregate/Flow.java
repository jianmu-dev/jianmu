package dev.jianmu.dsl.aggregate;

import dev.jianmu.task.aggregate.Definition;
import dev.jianmu.task.aggregate.ParameterRefer;
import dev.jianmu.task.aggregate.TaskParameter;
import dev.jianmu.version.aggregate.TaskDefinitionVersion;
import dev.jianmu.workflow.aggregate.definition.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @class: Workflow
 * @description: 流程
 * @author: Ethan Liu
 * @create: 2021-04-19 11:21
 **/
public class Flow {
    private final String name;
    private final String ref;
    private final String description;
    private final List<FlowNode> flowNodes = new ArrayList<>();
    private Set<Node> nodes;

    public Flow(Map<String, Object> flow) {
        this.name = (String) flow.get("name");
        this.ref = (String) flow.get("ref");
        this.description = (String) flow.get("description");
        flow.forEach((key, val) -> {
            if (val instanceof Map) {
                flowNodes.add(new FlowNode(key, (Map<?, ?>) val));
            }
        });
    }

    public void calculateNodes(List<TaskDefinitionVersion> taskDefinitionVersions) {
        // 创建节点
        Map<String, Node> symbolTable = new HashMap<>();
        flowNodes.forEach(flowNode -> {
            if (flowNode.getType().equals("start")) {
                var start = Start.Builder.aStart().name(flowNode.getName()).ref(flowNode.getName()).build();
                symbolTable.put(flowNode.getName(), start);
                return;
            }
            if (flowNode.getType().equals("end")) {
                var end = End.Builder.anEnd().name(flowNode.getName()).ref(flowNode.getName()).build();
                symbolTable.put(flowNode.getName(), end);
                return;
            }
            if (flowNode.getType().equals("condition")) {
                var cases = flowNode.getCases();
                Map<Boolean, String> targetMap = new HashMap<>();
                targetMap.put(true, cases.get("true"));
                targetMap.put(false, cases.get("false"));

                var condition = Condition.Builder.aCondition()
                        .name(flowNode.getName())
                        .ref(flowNode.getName())
                        .expression(flowNode.getExpression())
                        .targetMap(targetMap)
                        .build();
                condition.setTargets(Set.of(cases.get("true"), cases.get("false")));
                symbolTable.put(flowNode.getName(), condition);
                return;
            }
            // 创建任务节点
            var version = taskDefinitionVersions.stream()
                    .filter(taskDefinitionVersion -> taskDefinitionVersion.getDefinitionKey().equals(flowNode.getType()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("未找到任务定义"));
            var task = this.createAsyncTask(flowNode.getType(), flowNode.getName(), version);
            symbolTable.put(flowNode.getName(), task);
        });
        // 添加节点引用关系
        flowNodes.forEach(flowNode -> {
            var n = symbolTable.get(flowNode.getName());
            if (null != n) {
                flowNode.getTargets().forEach(nodeName -> {
                    var target = symbolTable.get(nodeName);
                    if (null != target) {
                        n.addTarget(target.getRef());
                    }
                });
                flowNode.getSources().forEach(nodeName -> {
                    var source = symbolTable.get(nodeName);
                    if (null != source) {
                        n.addSource(source.getRef());
                    }
                });
            }
        });
        this.nodes = new HashSet<>(symbolTable.values());
    }

    private AsyncTask createAsyncTask(String key, String nodeName, TaskDefinitionVersion version) {
        return AsyncTask.Builder.anAsyncTask()
                .name(version.getTaskDefinitionName())
                .ref(nodeName)
                .type(key)
                .description(version.getDescription())
                .build();
    }

    public Set<String> getAsyncTaskTypes() {
        return flowNodes.stream()
                .map(FlowNode::getType)
                .filter(type -> !type.equals("start"))
                .filter(type -> !type.equals("end"))
                .filter(type -> !type.equals("condition"))
                .collect(Collectors.toSet());
    }

    public List<ParameterRefer> getParameterRefers(String workflowVersion) {
        List<ParameterRefer> parameterRefers = new ArrayList<>();
        flowNodes.forEach(flowNode -> flowNode.getParam().forEach((key, val) -> {
            var outputVal = findOutputVariable(val);
            if (null != outputVal) {
                var strings = outputVal.split("\\.");
                var parameterRefer = ParameterRefer.Builder.aParameterRefer()
                        .sourceParameterRef(strings[1])
                        .sourceTaskRef(strings[0])
                        .targetParameterRef(key)
                        .targetTaskRef(flowNode.getName())
                        .workflowRef(this.ref)
                        .workflowVersion(workflowVersion)
                        .build();
                parameterRefers.add(parameterRefer);
            }
        }));
        return parameterRefers;
    }

    private String findOutputVariable(String paramValue) {
        Pattern pattern = Pattern.compile("^\\$\\{([a-zA-Z0-9_-]+\\.+[a-zA-Z0-9_-]*)\\}$");
        Matcher matcher = pattern.matcher(paramValue);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
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

    public List<FlowNode> getFlowNodes() {
        return flowNodes;
    }

    public Set<Node> getNodes() {
        return nodes;
    }
}
