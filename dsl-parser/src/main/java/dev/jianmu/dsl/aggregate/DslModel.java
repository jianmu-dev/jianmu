package dev.jianmu.dsl.aggregate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.jianmu.parameter.aggregate.Parameter;
import dev.jianmu.task.aggregate.Definition;
import dev.jianmu.task.aggregate.InputParameter;
import dev.jianmu.task.aggregate.TaskParameter;
import dev.jianmu.version.aggregate.TaskDefinitionVersion;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @class: DslModel
 * @description: DSL语法模型
 * @author: Ethan Liu
 * @create: 2021-04-16 20:29
 **/
public class DslModel {
    private String cron;
    private Map<String, Map<String, String>> event;
    private Map<String, String> param;
    private Map<String, Object> workflow;
    private Flow flow;
    private Set<DslParameter> dslParameters;
    private List<TaskDefinitionVersion> taskDefinitionVersions;
    private List<Definition> definitions;
    Map<DslParameter, TaskParameter> parameterMap = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    // DSL解析
    public static DslModel parse(String dslText) {
        DslModel dsl = new DslModel();
        try {
            dsl = dsl.mapper.readValue(dslText, DslModel.class);
        } catch (IOException e) {
            throw new RuntimeException("Dsl解析异常");
        }
        dsl.syntaxCheck();
        dsl.parseFlow();
        return dsl;
    }

    // 关系、参数计算
    public void calculate(List<Definition> definitions, List<TaskDefinitionVersion> taskDefinitionVersions) {
        this.definitions = List.copyOf(definitions);
        this.taskDefinitionVersions = List.copyOf(taskDefinitionVersions);
        this.flow.calculateNodes(this.taskDefinitionVersions);
        this.extractDslParameters();
        this.calculateParameters();
    }

    // 流程二次解析
    private void parseFlow() {
        this.flow = new Flow(this.workflow);
    }

    // DSL语法校验
    private void syntaxCheck() {
        if (this.cron.isBlank()) {
            throw new RuntimeException("Cron未设置");
        }
        if (null == this.workflow) {
            throw new RuntimeException("workflow未设置");
        }
        var flow = this.workflow;
        if (null == flow.get("name")) {
            throw new RuntimeException("workflow name未设置");
        }
        if (null == flow.get("ref")) {
            throw new RuntimeException("workflow ref未设置");
        }
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

    private void extractDslParameters() {
        Set<DslParameter> parameters = new HashSet<>();
        this.flow.getFlowNodes().forEach(flowNode -> flowNode.getParam().forEach((key, val) -> {
                    var valName = findVariable(val);
                    var secretName = findSecret(val);
                    if (null != valName) {
                        // 处理输入参数引用输出参数
                        if (valName.contains(".")) {
                            var strings = valName.split("\\.");
                            var p = DslParameter.Builder.aDslParameter()
                                    .nodeName(flowNode.getName())
                                    .definitionKey(flowNode.getType())
                                    .name(key)
                                    .outputNodeName(strings[0])
                                    .outputParameterName(strings[1])
                                    .build();
                            parameters.add(p);
                        }
                        // 全局参数合并
                        if (null != this.param.get(valName)) {
                            var p = DslParameter.Builder.aDslParameter()
                                    .nodeName(flowNode.getName())
                                    .definitionKey(flowNode.getType())
                                    .name(key)
                                    .value(this.param.get(valName))
                                    .build();
                            parameters.add(p);
                        }
                        return;
                    }
                    // 密钥类型参数
                    if (null != secretName) {
                        var p = DslParameter.Builder.aDslParameter()
                                .nodeName(flowNode.getName())
                                .definitionKey(flowNode.getType())
                                .name(key)
                                .value(secretName)
                                .build();
                        parameters.add(p);
                        return;
                    }
                    // 正常参数
                    var p = DslParameter.Builder.aDslParameter()
                            .nodeName(flowNode.getName())
                            .definitionKey(flowNode.getType())
                            .name(key)
                            .value(val)
                            .build();
                    parameters.add(p);
                })
        );
        this.dslParameters = parameters;
    }

    public void calculateParameters() {
        definitions.forEach(definition -> {
            dslParameters.forEach(dslParameter -> {
                // 如果dsl参数覆盖的是该任务定义输入参数
                if (dslParameter.getDefinitionKey().equals(definition.getKey())) {
                    definition.getInputParameterBy(dslParameter.getName()).ifPresent(taskParameter -> {
                        parameterMap.put(dslParameter, taskParameter);
                    });
                }
            });
        });
    }

    public Map<InputParameter, Parameter<?>> getInputParameterMap(String projectId, String WorkflowVersion) {
        return parameterMap.entrySet().stream()
                .map(entry -> {
                    var parameter = Parameter.Type.valueOf(entry.getValue().getType())
                            .newParameter(entry.getKey().getValue());
                    var inputParameter = InputParameter.Builder.anInputParameter()
                            .projectId(projectId)
                            .defKey(entry.getKey().getDefinitionKey())
                            .ref(entry.getKey().getName())
                            .asyncTaskRef(entry.getKey().getNodeName())
                            .workflowRef(this.flow.getRef())
                            .workflowVersion(WorkflowVersion)
                            .parameterId(parameter.getId())
                            .build();
                    return Map.entry(inputParameter, parameter);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public int getSteps() {
        return this.getFlow().getNodes().size() - 2;
    }

    public Flow getFlow() {
        return this.flow;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public Map<String, Map<String, String>> getEvent() {
        return event;
    }

    public void setEvent(Map<String, Map<String, String>> event) {
        this.event = event;
    }

    public Map<String, String> getParam() {
        return param;
    }

    public void setParam(Map<String, String> param) {
        this.param = param;
    }

    public Map<String, Object> getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Map<String, Object> workflow) {
        this.workflow = workflow;
    }
}
