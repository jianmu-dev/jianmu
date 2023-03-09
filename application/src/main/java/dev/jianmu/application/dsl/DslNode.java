package dev.jianmu.application.dsl;

import dev.jianmu.workflow.aggregate.definition.Branch;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Ethan Liu
 * @class Node
 * @description 节点
 * @create 2021-04-19 11:17
 */
@Getter
public class DslNode {
    private String name;
    private String alias;
    private String type;
    private String onFailure;
    private List<String> sources;
    private List<String> targets;
    private Map<String, String> param;
    private String expression;
    private List<Branch> branches;
    // Shell Node
    private String image;
    private Map<String, String> environment;
    private List<String> script;
    private Map<String, String> cache;

    public void setType(String type) {
        this.type = type;
    }

    private static DslNode shellNode(String nodeName, Map<?, ?> nodeMap) {
        var node = new DslNode();
        node.name = nodeName;
        var alias = (String) nodeMap.get("alias");
        node.alias = alias == null ? nodeName : alias;
        setRelation(nodeMap, node);
        node.image = (String) nodeMap.get("image");
        node.onFailure = (String) nodeMap.get("on-failure");

        var environment = nodeMap.get("environment");
        if (environment instanceof Map) {
            node.environment = ((Map<?, ?>) environment)
                    .entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .map(entry -> Map.entry((String) entry.getKey(), entry.getValue().toString()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } else {
            node.environment = Map.of();
        }
        var script = nodeMap.get("script");
        if (script instanceof List) {
            node.script = ((List<?>) script).stream().map(i -> (String) i).collect(Collectors.toList());
        } else if (script instanceof String) {
            node.script = List.of((String) script);
        } else {
            node.script = List.of();
        }

        var cache = nodeMap.get("cache");
        if (cache instanceof Map) {
            node.cache = ((Map<?, ?>) cache).entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .map(entry -> Map.entry((String) entry.getKey(), (String) entry.getValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        return node;
    }

    private static DslNode normalNode(String nodeName, Map<?, ?> node) {
        var dslNode = new DslNode();
        dslNode.name = nodeName;
        var alias = (String) node.get("alias");
        dslNode.alias = alias == null ? nodeName : alias;
        dslNode.type = (String) node.get("type");
        dslNode.onFailure = (String) node.get("on-failure");
        setRelation(node, dslNode);

        var p = node.get("param");
        if (p instanceof Map) {
            dslNode.param = ((Map<?, ?>) p)
                    .entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .map(entry -> {
                        if (entry.getValue() instanceof String) {
                            return Map.entry((String) entry.getKey(), (String) entry.getValue());
                        }
                        var value = "(" + entry.getValue().toString() + ")";
                        return Map.entry((String) entry.getKey(), value);
                    })
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } else {
            dslNode.param = Map.of();
        }
        var c = node.get("cases");
        if (c instanceof Map) {
            dslNode.branches = ((Map<?, ?>) c)
                    .entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .map(entry ->
                            Branch.Builder.aBranch()
                                    .matchedCondition(entry.getKey())
                                    .target((String) entry.getValue())
                                    .build()
                    )
                    .collect(Collectors.toList());
        } else {
            dslNode.branches = List.of();
        }
        var e = node.get("expression");
        if (e instanceof String) {
            dslNode.expression = (String) e;
        } else {
            dslNode.expression = "";
        }

        var cache = node.get("cache");
        if (cache instanceof Map) {
            dslNode.cache = ((Map<?, ?>) cache).entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .map(entry -> Map.entry((String) entry.getKey(), (String) entry.getValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        return dslNode;
    }

    private static void setRelation(Map<?, ?> node, DslNode dslNode) {
        var s = node.get("sources");
        if (s instanceof List) {
            dslNode.sources = ((List<?>) s).stream().map(i -> (String) i).collect(Collectors.toList());
        } else {
            dslNode.sources = List.of();
        }
        var t = node.get("targets");
        if (t instanceof List) {
            dslNode.targets = ((List<?>) t).stream().map(i -> (String) i).collect(Collectors.toList());
        } else {
            dslNode.targets = List.of();
        }
    }

    public static DslNode of(String nodeName, Map<?, ?> nodeMap) {
        var image = (String) nodeMap.get("image");
        if (image != null) {
            return DslNode.shellNode(nodeName, nodeMap);
        } else {
            return DslNode.normalNode(nodeName, nodeMap);
        }
    }

    private DslNode() {
    }
}
