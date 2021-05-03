package dev.jianmu.dsl.aggregate;

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

    public Set<String> getNodeTypes() {
        return nodes.stream()
                .map(Node::getType)
                .filter(type -> !type.equals("start"))
                .filter(type -> !type.equals("end"))
                .filter(type -> !type.equals("condition"))
                .collect(Collectors.toSet());
    }

    public Set<OutputParameterRefer> getOutputParameterRefs(String projectId, String workflowVersion) {
        Set<OutputParameterRefer> refers = new HashSet<>();
        nodes.forEach(node -> node.getParam().forEach((key, val) -> {
            var outputVal = findOutputVariable(val);
            if (null != outputVal) {
                var strings = outputVal.split("\\.");
                var refer = OutputParameterRefer.Builder.anOutputParameterRef()
                        .projectId(projectId)
                        .workflowVersion(workflowVersion)
                        .inputNodeName(node.getName())
                        .inputNodeType(node.getType())
                        .inputParameterRef(key)
                        .outputNodeName(strings[0])
                        .outputParameterRef(strings[1])
                        .build();
                refers.add(refer);
            }
        }));
        return refers;
    }

    public Set<DslParameter> getParams(Map<String, String> paramMap) {
        Set<DslParameter> parameters = new HashSet<>();
        nodes.forEach(node -> node.getParam().forEach((key, val) -> {
                    var valName = findVariable(val);
                    var secretName = findSecret(val);
                    if (null != valName) {
                        // 处理输入参数引用输出参数
                        if (valName.contains(".")) {
                            var strings = valName.split("\\.");
                            var p = DslParameter.Builder.aDslParameter()
                                    .nodeName(node.getName())
                                    .definitionKey(node.getType())
                                    .name(key)
                                    .outputNodeName(strings[0])
                                    .outputParameterName(strings[1])
                                    .build();
                            parameters.add(p);
                        }
                        // 全局参数合并
                        if (null != paramMap.get(valName)) {
                            var p = DslParameter.Builder.aDslParameter()
                                    .nodeName(node.getName())
                                    .definitionKey(node.getType())
                                    .name(key)
                                    .value(paramMap.get(valName))
                                    .build();
                            parameters.add(p);
                        }
                        return;
                    }
                    // 密钥类型参数
                    if (null != secretName) {
                        var p = DslParameter.Builder.aDslParameter()
                                .nodeName(node.getName())
                                .definitionKey(node.getType())
                                .name(key)
                                .value(secretName)
                                .build();
                        parameters.add(p);
                        return;
                    }
                    // 正常参数
                    var p = DslParameter.Builder.aDslParameter()
                            .nodeName(node.getName())
                            .definitionKey(node.getType())
                            .name(key)
                            .value(val)
                            .build();
                    parameters.add(p);
                })
        );
        return parameters;
    }

    private String findOutputVariable(String paramValue) {
        Pattern pattern = Pattern.compile("^\\$\\{([a-zA-Z0-9_-]+\\.+[a-zA-Z0-9_-]*)\\}$");
        Matcher matcher = pattern.matcher(paramValue);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String findVariable(String paramValue) {
        Pattern pattern = Pattern.compile("^\\$\\{([a-zA-Z0-9_-]+)\\}$");
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
