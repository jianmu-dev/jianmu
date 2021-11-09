package dev.jianmu.application.dsl;

import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @class: Node
 * @description: 节点
 * @author: Ethan Liu
 * @create: 2021-04-19 11:17
 **/
@Getter
public class DslNode {
    private String name;
    private String type;
    private List<String> sources;
    private List<String> targets;
    private Map<String, String> param;
    private String expression;
    private Map<String, String> cases;
    // Shell Node
    private String image;
    private Map<String, String> environment;
    private List<String> script;

    public void setType(String type) {
        this.type = type;
    }

    private static DslNode shellNode(String nodeName, Map<?, ?> nodeMap) {
        var node = new DslNode();
        node.name = nodeName;
        setRelation(nodeMap, node);
        node.image = (String) nodeMap.get("image");

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
        } else {
            node.script = List.of();
        }
        return node;
    }

    private static DslNode normalNode(String nodeName, Map<?, ?> node) {
        var dslNode = new DslNode();
        dslNode.name = nodeName;
        dslNode.type = (String) node.get("type");
        setRelation(node, dslNode);

        var p = node.get("param");
        if (p instanceof Map) {
            dslNode.param = ((Map<?, ?>) p)
                    .entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .map(entry -> Map.entry((String) entry.getKey(), entry.getValue().toString()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } else {
            dslNode.param = Map.of();
        }
        var c = node.get("cases");
        if (c instanceof Map) {
            dslNode.cases = ((Map<?, ?>) c)
                    .entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .map(entry -> Map.entry((String) entry.getKey(), (String) entry.getValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } else {
            dslNode.cases = Map.of();
        }
        var e = node.get("expression");
        if (e instanceof String) {
            dslNode.expression = (String) e;
        } else {
            dslNode.expression = "";
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
