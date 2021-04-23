package dev.jianmu.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.pagehelper.PageInfo;
import dev.jianmu.dsl.aggregate.DslModel;
import dev.jianmu.dsl.aggregate.DslParameter;
import dev.jianmu.dsl.aggregate.DslReference;
import dev.jianmu.dsl.aggregate.Flow;
import dev.jianmu.infrastructure.mybatis.dsl.DslReferenceRepositoryImpl;
import dev.jianmu.parameter.aggregate.Parameter;
import dev.jianmu.parameter.aggregate.Reference;
import dev.jianmu.parameter.repository.ParameterRepository;
import dev.jianmu.parameter.repository.ReferenceRepository;
import dev.jianmu.task.aggregate.TaskParameter;
import dev.jianmu.task.repository.DefinitionRepository;
import dev.jianmu.version.repository.TaskDefinitionRepository;
import dev.jianmu.version.repository.TaskDefinitionVersionRepository;
import dev.jianmu.workflow.aggregate.definition.*;
import dev.jianmu.workflow.repository.WorkflowRepository;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
    private final ParameterRepository parameterRepository;
    private final ReferenceRepository referenceRepository;
    private final DslReferenceRepositoryImpl dslReferenceRepository;
    private final WorkflowRepository workflowRepository;
    private final ObjectMapper mapper;

    public DslApplication(
            DefinitionRepository definitionRepository,
            TaskDefinitionRepository taskDefinitionRepository,
            TaskDefinitionVersionRepository taskDefinitionVersionRepository,
            ParameterRepository parameterRepository,
            ReferenceRepository referenceRepository,
            DslReferenceRepositoryImpl dslReferenceRepository,
            WorkflowRepository workflowRepository
    ) {
        this.definitionRepository = definitionRepository;
        this.taskDefinitionRepository = taskDefinitionRepository;
        this.taskDefinitionVersionRepository = taskDefinitionVersionRepository;
        this.parameterRepository = parameterRepository;
        this.referenceRepository = referenceRepository;
        this.dslReferenceRepository = dslReferenceRepository;
        this.workflowRepository = workflowRepository;
        this.mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
    }

    private DslModel parseDsl(File dslFile) {
        DslModel dsl = null;
        try {
            dsl = mapper.readValue(dslFile, DslModel.class);
        } catch (IOException e) {
            logger.error("Got error: ", e);
            throw new RuntimeException(e);
        }
        dsl.syntaxCheck();
        return dsl;
    }

    private AsyncTask createAsyncTask(String key, String nodeName) {
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
                .key(nodeName)
                .description(taskDefinitionVersion.getDescription())
                .build();
    }

    private Set<Node> createNodes(List<dev.jianmu.dsl.aggregate.Node> nodes) {
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
                Map<Boolean, String> targetMap = new HashMap<>();
                targetMap.put(true, cases.get("true"));
                targetMap.put(false, cases.get("false"));
                var condition = Condition.Builder.aCondition()
                        .name(node.getName())
                        .ref(node.getName())
                        .expression(node.getExpression())
                        .targetMap(targetMap)
                        .build();
                symbolTable.put(node.getName(), condition);
                return;
            }
            // 创建任务节点
            var task = this.createAsyncTask(node.getType(), node.getName());
            symbolTable.put(node.getName(), task);
        });
        // 添加节点引用关系
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

    private Map<String, Set<TaskParameter>> findDefinitionParameters(List<dev.jianmu.dsl.aggregate.Node> nodes) {
        Map<String, Set<TaskParameter>> parameters = new HashMap<>();
        nodes.stream()
                .filter(node -> !node.getType().equals("start"))
                .filter(node -> !node.getType().equals("end"))
                .filter(node -> !node.getType().equals("condition"))
                .filter(distinctByKey(dev.jianmu.dsl.aggregate.Node::getType))
                .forEach(node -> {
                    var definition = this.definitionRepository
                            .findByKey(node.getType())
                            .orElseThrow(() -> new RuntimeException("未找到任务定义"));
                    parameters.put(definition.getKey(), definition.getInputParameters());
                });
        return parameters;
    }

    private Map<DslParameter, Parameter> createParameters(Set<DslParameter> dslParameters, Map<String, Set<TaskParameter>> defParameterMap) {
        Map<DslParameter, Parameter> params = new HashMap<>();
        dslParameters.forEach(dslParameter -> {
            var parameters = defParameterMap.get(dslParameter.getDefinitionKey());
            if (null != parameters) {
                var r = parameters.stream()
                        .filter(p -> p.getRef().equals(dslParameter.getName()))
                        .findFirst();
                r.ifPresent(taskParameter -> {
                            var p = Parameter.Type.valueOf(taskParameter.getType())
                                    .newParameter(dslParameter.getValue());
                            dslParameter.setLinkedParameterId(taskParameter.getParameterId());
                            params.put(dslParameter, p);
                        }
                );
            }
        });
        return params;
    }

    @Transactional
    public Workflow importDsl(String dslUrl, Boolean update) {
        var dslFile = new File("test-dsl.yaml");
        // 解析DSL
        var dsl = this.parseDsl(dslFile);
        var flow = new Flow(dsl.getWorkflow());
        // 创建节点
        var nodes = this.createNodes(flow.getNodes());
        // 创建关联
        String dslText;
        try {
            dslText = FileUtils.readFileToString(dslFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("DSL Error: ", e);
            throw new RuntimeException("无法读取DSL");
        }
        var dslRef = this.dslReferenceRepository.findByWorkflowRef(flow.getRef()).orElse(
                DslReference.Builder.aReference()
                        .dslUrl("test-dsl.yaml")
                        .workflowName(flow.getName())
                        .workflowRef(flow.getRef())
                        .dslText(dslText)
                        .steps(nodes.size() - 2)
                        .lastModifiedBy("admin")
                        .build()
        );
        // 创建流程
        var workflow = Workflow.Builder.aWorkflow()
                .name(flow.getName())
                .ref(flow.getRef())
                .description(flow.getDescription())
                .version(dslRef.getWorkflowVersion())
                .nodes(nodes)
                .build();
        // 返回任务定义输入参数列表
        var parameters = this.findDefinitionParameters(flow.getNodes());
        var param = flow.getParams(dsl.getParam());
        // 创建参数Map
        var ps = this.createParameters(param, parameters);
        // 创建参数引用
        var refs = ps.entrySet().stream().map(entry ->
                Reference.Builder.aReference()
                        .contextId(dslRef.getId() + entry.getKey().getNodeName())
                        .parameterId(entry.getValue().getId())
                        .linkedParameterId(entry.getKey().getLinkedParameterId()).build()
        ).collect(Collectors.toList());

        // 保存
        if (update) {
            this.dslReferenceRepository.updateByWorkflowRef(dslRef);
        } else {
            this.dslReferenceRepository.add(dslRef);
        }
        this.workflowRepository.add(workflow);
        this.parameterRepository.addAll(new ArrayList<>(ps.values()));
        this.referenceRepository.addAll(refs);

        return workflow;
    }

    public void deleteById(String id) {
        DslReference dslReference = this.dslReferenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("未找到该DSL"));
        this.dslReferenceRepository.deleteByWorkflowRef(dslReference.getWorkflowRef());
        this.workflowRepository.deleteByRef(dslReference.getWorkflowRef());
    }

    public Optional<DslReference> findById(String dslId) {
        return this.dslReferenceRepository.findById(dslId);
    }

    public PageInfo<DslReference> findAll(int pageNum, int pageSize) {
        return this.dslReferenceRepository.findAll(pageNum, pageSize);
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
