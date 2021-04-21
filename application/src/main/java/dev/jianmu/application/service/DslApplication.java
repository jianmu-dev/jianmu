package dev.jianmu.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.jianmu.dsl.DslModel;
import dev.jianmu.dsl.Flow;
import dev.jianmu.task.repository.DefinitionRepository;
import dev.jianmu.version.repository.TaskDefinitionRepository;
import dev.jianmu.version.repository.TaskDefinitionVersionRepository;
import dev.jianmu.workflow.aggregate.definition.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @class: DslApplication
 * @description: DSL门面层
 * @author: Ethan Liu
 * @create: 2021-04-19 10:39
 **/
@Service
public class DslApplication {
    private static final Logger logger = LoggerFactory.getLogger(DslApplication.class);
    private final DefinitionRepository definitionRepository;
    private final TaskDefinitionRepository taskDefinitionRepository;
    private final TaskDefinitionVersionRepository taskDefinitionVersionRepository;

    public DslApplication(
            DefinitionRepository definitionRepository,
            TaskDefinitionRepository taskDefinitionRepository,
            TaskDefinitionVersionRepository taskDefinitionVersionRepository
    ) {
        this.definitionRepository = definitionRepository;
        this.taskDefinitionRepository = taskDefinitionRepository;
        this.taskDefinitionVersionRepository = taskDefinitionVersionRepository;
    }

    private DslModel parseDsl() {
        var mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        DslModel dsl = null;
        try {
            dsl = mapper.readValue(new File("test-dsl.yaml"), DslModel.class);
        } catch (IOException e) {
            logger.error("Got error: ", e);
            throw new RuntimeException(e);
        }
        dsl.syntaxCheck();
        return dsl;
    }

    private AsyncTask findDefinition(String key) {
        var definition = this.definitionRepository
                .findByKey(key)
                .orElseThrow(() -> new RuntimeException("未找到任务定义"));
        var taskDefinitionVersion = this.taskDefinitionVersionRepository
                .findByDefinitionKey(key)
                .orElseThrow(() -> new RuntimeException("未找到任务定义版本"));
        var taskDefinition = this.taskDefinitionRepository
                .findByRef(taskDefinitionVersion.getTaskDefinitionRef())
                .orElseThrow(() -> new RuntimeException("未找到任务定义"));
        return AsyncTask.Builder.anAsyncTask()
                .name(taskDefinition.getName())
                .ref(definition.getKey())
                .description(taskDefinitionVersion.getDescription())
                .build();
    }

    private Set<Node> createNodes(List<dev.jianmu.dsl.Node> nodes) {
        // 创建节点
        Map<String, Node> symbolTable = new HashMap<>();
        nodes.forEach(node -> {
            if (node.getType().equals("start")) {
                var start = Start.Builder.aStart().name(node.getName()).ref(node.getName()).build();
                symbolTable.put(node.getName(), start);
                return;
            }
            if (node.getType().equals("end")) {
                var end = End.Builder.anEnd().name(node.getName()).ref(node.getName()).build();
                symbolTable.put(node.getName(), end);
                return;
            }
            if (node.getType().equals("condition")) {
                var cases = node.getCases();
                var condition = Condition.Builder.aCondition()
                        .name(node.getName())
                        .ref(node.getName())
                        .expression(node.getExpression())
                        .targetMap(Map.of(true, cases.get("true"), false, cases.get("false")))
                        .build();
                symbolTable.put(node.getName(), condition);
                return;
            }
            // 创建任务节点
            var task = this.findDefinition(node.getType());
            symbolTable.put(node.getName(), task);
        });
        nodes.forEach(node -> {
            var n = symbolTable.get(node.getName());
            if (null != n) {
                node.getTargets().forEach(nodeName -> {
                    var target = symbolTable.get(nodeName);
                    if (null != target) {
                        n.addTarget(target.getRef());
                    }
                });
                node.getSources().forEach(nodeName -> {
                    var source = symbolTable.get(nodeName);
                    if (null != source) {
                        n.addSource(source.getRef());
                    }
                });
            }
        });
        return new HashSet<>(symbolTable.values());
    }

    public Workflow importDsl() {
        var dsl = this.parseDsl();
        var flow = new Flow(dsl.getWorkflow());
        // 创建节点
        var nodes = this.createNodes(flow.getNodes());
        // 覆盖全局参数
        var param = flow.getParams(dsl.getParam());
        param.forEach((key, val) -> {
            logger.info(key);
            logger.info(val);
        });
        // 创建流程
        return Workflow.Builder.aWorkflow()
                .name(flow.getName())
                .ref(flow.getRef())
                .description(flow.getDescription())
                // TODO 处理版本号
                .version("1.0")
                .nodes(nodes)
                .build();
    }
}
