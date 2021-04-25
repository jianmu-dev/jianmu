package dev.jianmu.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.pagehelper.PageInfo;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.dsl.aggregate.*;
import dev.jianmu.dsl.repository.DslSourceCodeRepository;
import dev.jianmu.infrastructure.mybatis.dsl.ProjectRepositoryImpl;
import dev.jianmu.parameter.aggregate.Parameter;
import dev.jianmu.parameter.aggregate.Reference;
import dev.jianmu.parameter.repository.ParameterRepository;
import dev.jianmu.parameter.repository.ReferenceRepository;
import dev.jianmu.task.aggregate.TaskParameter;
import dev.jianmu.task.repository.DefinitionRepository;
import dev.jianmu.version.repository.TaskDefinitionRepository;
import dev.jianmu.version.repository.TaskDefinitionVersionRepository;
import dev.jianmu.workflow.aggregate.definition.Node;
import dev.jianmu.workflow.aggregate.definition.*;
import dev.jianmu.workflow.repository.WorkflowRepository;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ProjectRepositoryImpl projectRepository;
    private final DslSourceCodeRepository dslSourceCodeRepository;
    private final WorkflowRepository workflowRepository;
    private final ObjectMapper mapper;
    private final ApplicationEventPublisher publisher;

    public DslApplication(
            DefinitionRepository definitionRepository,
            TaskDefinitionRepository taskDefinitionRepository,
            TaskDefinitionVersionRepository taskDefinitionVersionRepository,
            ParameterRepository parameterRepository,
            ReferenceRepository referenceRepository,
            ProjectRepositoryImpl projectRepository,
            DslSourceCodeRepository dslSourceCodeRepository,
            WorkflowRepository workflowRepository,
            ApplicationEventPublisher publisher
    ) {
        this.definitionRepository = definitionRepository;
        this.taskDefinitionRepository = taskDefinitionRepository;
        this.taskDefinitionVersionRepository = taskDefinitionVersionRepository;
        this.parameterRepository = parameterRepository;
        this.referenceRepository = referenceRepository;
        this.projectRepository = projectRepository;
        this.dslSourceCodeRepository = dslSourceCodeRepository;
        this.workflowRepository = workflowRepository;
        this.publisher = publisher;
        this.mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
    }

    public void trigger(String dslId) {
        var dslRef = this.projectRepository.findById(dslId)
                .orElseThrow(() -> new DataNotFoundException("未找到该DSL"));
        publisher.publishEvent(dslRef);
    }

    @Transactional
    public Workflow importDsl(String dslUrl) {
        var dslFile = new File(dslUrl);
        String dslText;
        try {
            dslText = FileUtils.readFileToString(dslFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("DSL Error: ", e);
            throw new RuntimeException("无法读取DSL");
        }
        // 解析DSL
        var dsl = this.parseDsl(dslText);
        var flow = new Flow(dsl.getWorkflow());
        // 创建节点
        var nodes = this.createNodes(flow.getNodes());
        // 创建关联
        var project = Project.Builder.aReference()
                .dslUrl("test-dsl.yaml")
                .workflowName(flow.getName())
                .workflowRef(flow.getRef())
                .dslText(dslText)
                .steps(nodes.size() - 2)
                .lastModifiedBy("admin")
                .build();
        // 返回任务定义输入输出参数列表
        var parameters = this.findDefinitionParameters(flow.getNodes());
        // 返回DSL定义参数列表
        var param = flow.getParams(dsl.getParam());
        // 返回DSL定义输出参数引用关系
        var outputRefs = flow.getOutputParameterRefs(project.getId(), project.getWorkflowVersion());
        // 创建参数Map
        var ps = this.createParameters(param, parameters);
        // 创建参数引用,使用project id + WorkflowVersion + NodeName作为参数引用contextId，参见WorkerApplication#getEnvironmentMap
        var refs = this.createRefs(ps, project.getId() + project.getWorkflowVersion());
        // 创建输入参数到输出参数的参数引用
        var outRefs = this.createInputOutputRefs(outputRefs, parameters);
        // 创建流程
        var workflow = Workflow.Builder.aWorkflow()
                .name(flow.getName())
                .ref(flow.getRef())
                .description(flow.getDescription())
                .version(project.getWorkflowVersion())
                .nodes(nodes)
                .build();
        // 保存原始DSL
        var dslSource = DslSourceCode.Builder.aDslSourceCode()
                .projectId(project.getId())
                .workflowRef(workflow.getRef())
                .workflowVersion(workflow.getVersion())
                .dslText(dslText)
                .build();
        // 保存
        this.projectRepository.add(project);
        this.dslSourceCodeRepository.add(dslSource);
        this.workflowRepository.add(workflow);
        this.parameterRepository.addAll(new ArrayList<>(ps.values()));
        // 合并保存参数引用
        refs.addAll(outRefs);
        this.referenceRepository.addAll(refs);

        return workflow;
    }

    @Transactional
    public Workflow syncDsl(String dslId) {
        Project project = this.projectRepository.findById(dslId)
                .orElseThrow(() -> new DataNotFoundException("未找到该DSL"));
        var dslFile = new File(project.getDslUrl());
        String dslText;
        try {
            dslText = FileUtils.readFileToString(dslFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("DSL Error: ", e);
            throw new RuntimeException("无法读取DSL");
        }
        // 解析DSL
        var dsl = this.parseDsl(dslText);
        var flow = new Flow(dsl.getWorkflow());
        // 创建节点
        var nodes = this.createNodes(flow.getNodes());
        project.setDslText(dslText);
        project.setLastModifiedBy("admin");
        project.setSteps(nodes.size() - 2);
        project.setWorkflowName(flow.getName());
        project.setLastModifiedTime();
        project.setWorkflowVersion();
        // 创建流程
        var workflow = Workflow.Builder.aWorkflow()
                .name(flow.getName())
                .ref(flow.getRef())
                .description(flow.getDescription())
                .version(project.getWorkflowVersion())
                .nodes(nodes)
                .build();
        // 返回任务定义输入输出参数列表
        var parameters = this.findDefinitionParameters(flow.getNodes());
        // 返回DSL定义参数列表
        var param = flow.getParams(dsl.getParam());
        // 返回DSL定义输出参数引用关系
        var outputRefs = flow.getOutputParameterRefs(project.getId(), project.getWorkflowVersion());
        outputRefs.forEach(System.out::println);
        // 创建参数Map
        var ps = this.createParameters(param, parameters);
        // 创建参数引用,使用project id + WorkflowVersion + NodeName作为参数引用contextId，参见WorkerApplication#getEnvironmentMap
        var refs = this.createRefs(ps, project.getId() + project.getWorkflowVersion());
        // 创建输入参数到输出参数的参数引用
        var outRefs = this.createInputOutputRefs(outputRefs, parameters);
        // 保存原始DSL
        var dslSource = DslSourceCode.Builder.aDslSourceCode()
                .projectId(project.getId())
                .workflowRef(workflow.getRef())
                .workflowVersion(workflow.getVersion())
                .dslText(dslText)
                .build();

        // 保存
        this.projectRepository.updateByWorkflowRef(project);
        this.dslSourceCodeRepository.add(dslSource);
        this.workflowRepository.add(workflow);
        this.parameterRepository.addAll(new ArrayList<>(ps.values()));
        // 合并保存参数引用
        refs.addAll(outRefs);
        this.referenceRepository.addAll(refs);

        return workflow;
    }

    public void deleteById(String id) {
        Project project = this.projectRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("未找到该DSL"));
        this.projectRepository.deleteByWorkflowRef(project.getWorkflowRef());
        this.workflowRepository.deleteByRef(project.getWorkflowRef());
    }

    public DslSourceCode findByRefAndVersion(String ref, String version) {
        return this.dslSourceCodeRepository.findByRefAndVersion(ref, version).orElseThrow(() -> new DataNotFoundException("未找到该DSL"));
    }

    public PageInfo<Project> findAll(int pageNum, int pageSize) {
        return this.projectRepository.findAll(pageNum, pageSize);
    }

    private DslModel parseDsl(String dslText) {
        DslModel dsl;
        try {
            dsl = mapper.readValue(dslText, DslModel.class);
        } catch (IOException e) {
            logger.error("Got error: ", e);
            throw new RuntimeException("Dsl解析异常");
        }
        dsl.syntaxCheck();
        return dsl;
    }

    private AsyncTask createAsyncTask(String key, String nodeName) {
        var definition = this.definitionRepository
                .findByKey(key)
                .orElseThrow(() -> new DataNotFoundException("未找到任务定义"));
        var taskDefinitionVersion = this.taskDefinitionVersionRepository
                .findByDefinitionKey(key)
                .orElseThrow(() -> new DataNotFoundException("未找到任务定义版本"));
        var taskDefinition = this.taskDefinitionRepository
                .findByRef(taskDefinitionVersion.getTaskDefinitionRef())
                .orElseThrow(() -> new DataNotFoundException("未找到任务定义"));
        return AsyncTask.Builder.anAsyncTask()
                .name(taskDefinition.getName())
                .ref(nodeName)
                .type(definition.getKey())
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
                condition.setTargets(Set.of(cases.get("true"), cases.get("false")));
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
                            .orElseThrow(() -> new DataNotFoundException("未找到任务定义"));
                    parameters.put(definition.getKey(), definition.getInputParameters());
                    parameters.put(node.getName(), definition.getOutputParameters());
                });
        return parameters;
    }

    private Map<DslParameter, Parameter> createParameters(Set<DslParameter> dslParameters, Map<String, Set<TaskParameter>> defParameterMap) {
        Map<DslParameter, Parameter> params = new HashMap<>();
        dslParameters.forEach(dslParameter -> {
            // 正常覆盖输入参数
            var parameters = defParameterMap.get(dslParameter.getDefinitionKey());
            if (null != parameters) {
                var taskParameterOptional = parameters.stream()
                        .filter(p -> p.getRef().equals(dslParameter.getName()))
                        .findFirst();
                taskParameterOptional.ifPresent(taskParameter -> {
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

    private List<Reference> createInputOutputRefs(Set<OutputParameterRefer> outputParameterRefers, Map<String, Set<TaskParameter>> defParameterMap) {
        return outputParameterRefers.stream()
                .map(outputParameterRefer -> {
                    var inputParameters = defParameterMap.get(outputParameterRefer.getInputNodeType());
                    var inputParameter = inputParameters.stream()
                            .filter(p -> p.getRef().equals(outputParameterRefer.getInputParameterRef()))
                            .findFirst().orElseThrow(() -> new DataNotFoundException("输入参数不存在"));
                    var outputParameters = defParameterMap.get(outputParameterRefer.getOutputNodeName());
                    var outParameter = outputParameters.stream()
                            .filter(p -> p.getRef().equals(outputParameterRefer.getOutputParameterRef()))
                            .findFirst().orElseThrow(() -> new DataNotFoundException("被引用的输出参数不存在"));
                    outputParameterRefer.setOutputParameterId(outParameter.getParameterId());
                    outputParameterRefer.setInputParameterId(inputParameter.getParameterId());
                    return Reference.Builder.aReference()
                            // contextId规则,使用project id + WorkflowVersion + NodeName
                            .contextId(outputParameterRefer.getProjectId() + outputParameterRefer.getWorkflowVersion() + outputParameterRefer.getInputNodeName())
                            .parameterId(inputParameter.getParameterId())
                            .linkedParameterId(outParameter.getParameterId()).build();
                }).collect(Collectors.toList());
    }

    private List<Reference> createRefs(Map<DslParameter, Parameter> ps, String contextId) {
        return ps.entrySet().stream().map(entry ->
                Reference.Builder.aReference()
                        .contextId(contextId + entry.getKey().getNodeName())
                        .parameterId(entry.getValue().getId())
                        .linkedParameterId(entry.getKey().getLinkedParameterId()).build()
        ).collect(Collectors.toList());
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
