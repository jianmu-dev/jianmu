package dev.jianmu.dsl.aggregate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @class: Node
 * @description: 节点
 * @author: Ethan Liu
 * @create: 2021-04-19 11:17
 **/
public class Node {
    private final String name;
    private final String type;
    private final List<String> sources;
    private final List<String> targets;
    private final Map<String, String> param;
    private final String expression;
    private final Map<String, String> cases;

    public Node(String nodeName, Map<?, ?> node) {
        this.name = nodeName;
        this.type = (String) node.get("type");
        var s = node.get("sources");
        if (s instanceof List) {
            this.sources = ((List<?>) s).stream().map(i -> (String) i).collect(Collectors.toList());
        } else {
            this.sources = List.of();
        }
        var t = node.get("targets");
        if (t instanceof List) {
            this.targets = ((List<?>) t).stream().map(i -> (String) i).collect(Collectors.toList());
        } else {
            this.targets = List.of();
        }
        var p = node.get("param");
        if (p instanceof Map) {
            this.param = ((Map<?, ?>) p)
                    .entrySet().stream()
                    .map(entry -> Map.entry((String) entry.getKey(), (String) entry.getValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } else {
            this.param = Map.of();
        }
        var c = node.get("cases");
        if (c instanceof Map) {
            this.cases = ((Map<?, ?>) c)
                    .entrySet().stream()
                    .map(entry -> Map.entry((String) entry.getKey(), (String) entry.getValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } else {
            this.cases = Map.of();
        }
        var e = node.get("expression");
        if (e instanceof String) {
            this.expression = (String) e;
        } else {
            this.expression = "";
        }
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public List<String> getSources() {
        return sources;
    }

    public List<String> getTargets() {
        return targets;
    }

    public Map<String, String> getParam() {
        return param;
    }

    public String getExpression() {
        return expression;
    }

    public Map<String, String> getCases() {
        return cases;
    }
}
