package dev.jianmu.application.service;

import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.dsl.aggregate.DslModel;
import dev.jianmu.dsl.aggregate.Flow;
import dev.jianmu.dsl.aggregate.FlowNode;
import dev.jianmu.infrastructure.jgit.JgitService;
import dev.jianmu.infrastructure.mybatis.dsl.ProjectRepositoryImpl;
import dev.jianmu.parameter.aggregate.Parameter;
import dev.jianmu.parameter.repository.ParameterRepository;
import dev.jianmu.project.aggregate.DslSourceCode;
import dev.jianmu.project.aggregate.GitRepo;
import dev.jianmu.project.aggregate.Project;
import dev.jianmu.project.repository.DslSourceCodeRepository;
import dev.jianmu.project.repository.GitRepoRepository;
import dev.jianmu.task.aggregate.InputParameter;
import dev.jianmu.task.aggregate.ParameterRefer;
import dev.jianmu.task.repository.DefinitionRepository;
import dev.jianmu.task.repository.InputParameterRepository;
import dev.jianmu.task.repository.ParameterReferRepository;
import dev.jianmu.version.repository.TaskDefinitionRepository;
import dev.jianmu.version.repository.TaskDefinitionVersionRepository;
import dev.jianmu.workflow.aggregate.definition.*;
import dev.jianmu.workflow.repository.WorkflowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    private final InputParameterRepository inputParameterRepository;
    private final ParameterReferRepository parameterReferRepository;
    private final ProjectRepositoryImpl projectRepository;
    private final DslSourceCodeRepository dslSourceCodeRepository;
    private final GitRepoRepository gitRepoRepository;
    private final WorkflowRepository workflowRepository;
    private final ApplicationEventPublisher publisher;
    private final JgitService jgitService;

    public DslApplication(
            DefinitionRepository definitionRepository,
            TaskDefinitionRepository taskDefinitionRepository,
            TaskDefinitionVersionRepository taskDefinitionVersionRepository,
            ParameterRepository parameterRepository,
            InputParameterRepository inputParameterRepository,
            ParameterReferRepository parameterReferRepository,
            ProjectRepositoryImpl projectRepository,
            DslSourceCodeRepository dslSourceCodeRepository,
            GitRepoRepository gitRepoRepository,
            WorkflowRepository workflowRepository,
            ApplicationEventPublisher publisher,
            JgitService jgitService
    ) {
        this.definitionRepository = definitionRepository;
        this.taskDefinitionRepository = taskDefinitionRepository;
        this.taskDefinitionVersionRepository = taskDefinitionVersionRepository;
        this.parameterRepository = parameterRepository;
        this.inputParameterRepository = inputParameterRepository;
        this.parameterReferRepository = parameterReferRepository;
        this.projectRepository = projectRepository;
        this.dslSourceCodeRepository = dslSourceCodeRepository;
        this.gitRepoRepository = gitRepoRepository;
        this.workflowRepository = workflowRepository;
        this.publisher = publisher;
        this.jgitService = jgitService;
    }

    public void importProject(GitRepo gitRepo) {
        var dslText = this.jgitService.readDsl(gitRepo.getId(), gitRepo.getDslPath());
        this.createProject(dslText, gitRepo.getId());
        this.gitRepoRepository.add(gitRepo);
        this.jgitService.cleanUp(gitRepo.getId());
    }

    public void syncProject(String projectId) {
        logger.info("开始同步Git仓库中的DSL");
        var project = this.projectRepository.findById(projectId)
                .orElseThrow(() -> new DataNotFoundException("未找到该项目"));
        var gitRepo = this.gitRepoRepository.findById(project.getGitRepoId())
                .orElseThrow(() -> new DataNotFoundException("未找到Git仓库配置"));
        var dslText = this.jgitService.readDsl(gitRepo.getId(), gitRepo.getDslPath());
        this.updateProject(projectId, dslText);
        this.jgitService.cleanUp(gitRepo.getId());
    }

    public void createProject(String dslText, String gitRepoId) {
        // 解析DSL
        var dsl = DslModel.parse(dslText);
        var flow = dsl.getFlow();
        // 创建节点
        var nodes = this.createNodes(flow.getNodes());
        // 创建项目
        var project = Project.Builder.aReference()
                .dslSource((gitRepoId == null) ? Project.DslSource.LOCAL : Project.DslSource.GIT)
                .gitRepoId(gitRepoId)
                .workflowName(flow.getName())
                .workflowRef(flow.getRef())
                .dslText(dslText)
                .steps(nodes.size() - 2)
                .lastModifiedBy("admin")
                .build();
        this.projectRepository.add(project);
        this.createWorkflow(project, dsl, dslText);
    }

    public void updateProject(String dslId, String dslText) {
        Project project = this.projectRepository.findById(dslId)
                .orElseThrow(() -> new DataNotFoundException("未找到该DSL"));
        // 解析DSL
        var dsl = DslModel.parse(dslText);
        var flow = new Flow(dsl.getWorkflow());
        // 创建节点
        var nodes = this.createNodes(flow.getNodes());
        project.setDslText(dslText);
        project.setLastModifiedBy("admin");
        project.setSteps(nodes.size() - 2);
        project.setWorkflowName(flow.getName());
        project.setLastModifiedTime();
        project.setWorkflowVersion();
        this.projectRepository.updateByWorkflowRef(project);
        this.createWorkflow(project, dsl, dslText);
    }

    @Transactional
    public void createWorkflow(Project project, DslModel dsl, String dslText) {
        // 创建节点
        var flow = new Flow(dsl.getWorkflow());
        var nodes = this.createNodes(flow.getNodes());
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
                .lastModifiedBy("admin")
                .build();
        // DSL全局变量处理后的参数列表
        var dslParameters = flow.getParams(dsl.getParam());
        // 查找DSL中指定了参数的任务定义列表
        var definitionMap = flow.getNodeTypes().stream()
                .filter(key -> dslParameters.stream().anyMatch(dslParameter -> dslParameter.getDefinitionKey().equals(key)))
                .map(key -> {
                    var definition = this.definitionRepository
                            .findByKey(key)
                            .orElseThrow(() -> new DataNotFoundException("未找到任务定义"));
                    return Map.entry(key, definition);
                }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        // 根据DSL中指定参数的参数名在任务定义的输入参数中匹配，并返回匹配的参数Map
        var parameterMap = dslParameters.stream()
                .filter(dslParameter ->
                        // 由于上面已经验证了任务定义，因此这里的Map#get不会有空值
                        definitionMap.get(dslParameter.getDefinitionKey())
                                .getInputParameterBy(dslParameter.getName()).isPresent())
                .map(dslParameter -> Map.entry(dslParameter, definitionMap.get(dslParameter.getDefinitionKey())
                        // 由于上面已经验证了参数定义是否存在，因此这里的Optional#get不会有空值
                        .getInputParameterBy(dslParameter.getName()).get()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        // 将parameterMap转变为 InputParameter与Parameter参数Map
        var inputParameterMap = parameterMap.entrySet().stream()
                .map(entry -> {
                    var parameter = Parameter.Type.valueOf(entry.getValue().getType())
                            .newParameter(entry.getKey().getValue());
                    var inputParameter = InputParameter.Builder.anInputParameter()
                            .projectId(project.getId())
                            .defKey(entry.getKey().getDefinitionKey())
                            .ref(entry.getKey().getName())
                            .asyncTaskRef(entry.getKey().getNodeName())
                            .workflowRef(project.getWorkflowRef())
                            .workflowVersion(project.getWorkflowVersion())
                            .parameterId(parameter.getId())
                            .build();
                    return Map.entry(inputParameter, parameter);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        // 根据 DSL定义outputRefs 创建 ParameterRefer
        var outputRefs = flow.getOutputParameterRefs(project.getId(), project.getWorkflowVersion());
        var parameterRefers = outputRefs.stream()
                .map(outputParameterRefer -> ParameterRefer.Builder.aParameterRefer()
                        .sourceParameterRef(outputParameterRefer.getOutputParameterRef())
                        .sourceTaskRef(outputParameterRefer.getOutputNodeName())
                        .targetParameterRef(outputParameterRefer.getInputParameterRef())
                        .targetTaskRef(outputParameterRefer.getInputNodeName())
                        .workflowRef(project.getWorkflowRef())
                        .workflowVersion(project.getWorkflowVersion())
                        .build())
                .collect(Collectors.toList());
        this.inputParameterRepository.addAll(new ArrayList<>(inputParameterMap.keySet()));
        this.parameterRepository.addAll(new ArrayList<>(inputParameterMap.values()));
        this.dslSourceCodeRepository.add(dslSource);
        this.workflowRepository.add(workflow);
        this.parameterReferRepository.addAll(parameterRefers);
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

    private Set<Node> createNodes(List<FlowNode> flowNodes) {
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
            var task = this.createAsyncTask(flowNode.getType(), flowNode.getName());
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
        return new HashSet<>(symbolTable.values());
    }
}
