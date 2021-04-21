package dev.jianmu.dsl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private final List<Node> nodes = new ArrayList<>();

    public Flow(Map<String, Object> flow) {
        this.name = (String) flow.get("name");
        this.ref = (String) flow.get("ref");
        this.description = (String) flow.get("description");
        flow.forEach((key, val) -> {
            if (val instanceof Map) {
                nodes.add(new Node(key, (Map<?, ?>) val));
            }
        });
    }

    public Map<String, String> getParams(Map<String, String> paramMap) {
        Map<String, String> newParamMap = new HashMap<>();
        nodes.forEach(node -> node.getParam().forEach((key, val) -> {
                    var valName = findVariable(val);
                    var secretName = findSecret(val);
                    if (null != valName) {
                        if (null != paramMap.get(valName)) {
                            newParamMap.put(node.getName() + "_" + key, paramMap.get(valName));
                        }
                        return;
                    }
                    if (null != secretName) {
                        newParamMap.put(node.getName() + "_" + key, secretName);
                        return;
                    }
                    newParamMap.put(node.getName() + "_" + key, val);
                })
        );
        return newParamMap;
    }

    private String findVariable(String paramValue) {
        Pattern pattern = Pattern.compile("^\\$\\{([a-zA-Z0-9_-]+\\.*[a-zA-Z0-9_-]*)\\}$");
        Matcher matcher = pattern.matcher(paramValue);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String findSecret(String paramValue) {
        Pattern pattern = Pattern.compile("^\\(\\(([a-zA-Z0-9_-]+\\.*[a-zA-Z0-9_-]+)\\)\\)$");
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

    public List<Node> getNodes() {
        return nodes;
    }
}
