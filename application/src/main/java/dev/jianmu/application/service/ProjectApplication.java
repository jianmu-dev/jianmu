package dev.jianmu.application.service;

import com.github.pagehelper.PageInfo;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.dsl.aggregate.DslModel;
import dev.jianmu.infrastructure.jgit.JgitService;
import dev.jianmu.infrastructure.mybatis.project.ProjectRepositoryImpl;
import dev.jianmu.parameter.repository.ParameterRepository;
import dev.jianmu.project.aggregate.CronTrigger;
import dev.jianmu.project.aggregate.DslSourceCode;
import dev.jianmu.project.aggregate.GitRepo;
import dev.jianmu.project.aggregate.Project;
import dev.jianmu.project.repository.CronTriggerRepository;
import dev.jianmu.project.repository.DslSourceCodeRepository;
import dev.jianmu.project.repository.GitRepoRepository;
import dev.jianmu.task.aggregate.Definition;
import dev.jianmu.task.repository.DefinitionRepository;
import dev.jianmu.task.repository.InputParameterRepository;
import dev.jianmu.task.repository.ParameterReferRepository;
import dev.jianmu.trigger.service.ScheduleJobService;
import dev.jianmu.version.aggregate.TaskDefinitionVersion;
import dev.jianmu.version.repository.TaskDefinitionVersionRepository;
import dev.jianmu.workflow.aggregate.definition.Workflow;
import dev.jianmu.workflow.repository.WorkflowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @class: ProjectApplication
 * @description: ProjectApplication
 * @author: Ethan Liu
 * @create: 2021-05-15 22:13
 **/
@Service
public class ProjectApplication {
    private static final Logger logger = LoggerFactory.getLogger(ProjectApplication.class);

    private final ProjectRepositoryImpl projectRepository;
    private final DslSourceCodeRepository dslSourceCodeRepository;
    private final CronTriggerRepository cronTriggerRepository;
    private final ScheduleJobService scheduleJobService;
    private final DefinitionRepository definitionRepository;
    private final TaskDefinitionVersionRepository taskDefinitionVersionRepository;
    private final GitRepoRepository gitRepoRepository;
    private final WorkflowRepository workflowRepository;
    private final ParameterRepository parameterRepository;
    private final InputParameterRepository inputParameterRepository;
    private final ParameterReferRepository parameterReferRepository;
    private final ApplicationEventPublisher publisher;
    private final JgitService jgitService;

    public ProjectApplication(
            ProjectRepositoryImpl projectRepository,
            DslSourceCodeRepository dslSourceCodeRepository,
            CronTriggerRepository cronTriggerRepository,
            ScheduleJobService scheduleJobService,
            DefinitionRepository definitionRepository,
            TaskDefinitionVersionRepository taskDefinitionVersionRepository,
            GitRepoRepository gitRepoRepository,
            WorkflowRepository workflowRepository,
            ParameterRepository parameterRepository,
            InputParameterRepository inputParameterRepository,
            ParameterReferRepository parameterReferRepository,
            ApplicationEventPublisher publisher,
            JgitService jgitService
    ) {
        this.projectRepository = projectRepository;
        this.dslSourceCodeRepository = dslSourceCodeRepository;
        this.cronTriggerRepository = cronTriggerRepository;
        this.scheduleJobService = scheduleJobService;
        this.definitionRepository = definitionRepository;
        this.taskDefinitionVersionRepository = taskDefinitionVersionRepository;
        this.gitRepoRepository = gitRepoRepository;
        this.workflowRepository = workflowRepository;
        this.parameterRepository = parameterRepository;
        this.inputParameterRepository = inputParameterRepository;
        this.parameterReferRepository = parameterReferRepository;
        this.publisher = publisher;
        this.jgitService = jgitService;
    }

    public void trigger(String projectId) {
        var project = this.projectRepository.findById(projectId)
                .orElseThrow(() -> new DataNotFoundException("未找到该项目"));
        this.publisher.publishEvent(project);
    }

    public void triggerFromCron(String triggerId) {
        var cronTrigger = this.cronTriggerRepository.findById(triggerId)
                .orElseThrow(() -> new DataNotFoundException("未找到该触发器"));
        this.trigger(cronTrigger.getProjectId());
    }

    private DslModel parseDsl(String dslText) {
        // 解析DSL
        var dsl = DslModel.parse(dslText);
        // 校验任务类型是否存在并创建流程节点关系
        var types = dsl.getFlow().getAsyncTaskTypes();
        List<TaskDefinitionVersion> versions = new ArrayList<>();
        List<Definition> definitions = new ArrayList<>();
        types.forEach(type -> {
            var v = this.taskDefinitionVersionRepository
                    .findByDefinitionKey(type)
                    .orElseThrow(() -> new DataNotFoundException("未找到任务定义版本"));
            var definition = this.definitionRepository
                    .findByKey(type)
                    .orElseThrow(() -> new DataNotFoundException("未找到任务定义"));
            versions.add(v);
            definitions.add(definition);
        });
        dsl.calculate(definitions, versions);
        return dsl;
    }

    private Workflow createWorkflow(DslModel dsl, String workflowVersion) {
        return Workflow.Builder.aWorkflow()
                .name(dsl.getFlow().getName())
                .ref(dsl.getFlow().getRef())
                .description(dsl.getFlow().getDescription())
                .version(workflowVersion)
                .nodes(dsl.getFlow().getNodes())
                .build();
    }

    private DslSourceCode createDslSourceCode(String projectId, Workflow workflow, String dslText) {
        return DslSourceCode.Builder.aDslSourceCode()
                .projectId(projectId)
                .workflowRef(workflow.getRef())
                .workflowVersion(workflow.getVersion())
                .dslText(dslText)
                .lastModifiedBy("admin")
                .build();
    }

    @Transactional
    public void importProject(GitRepo gitRepo) {
        var dslText = this.jgitService.readDsl(gitRepo.getId(), gitRepo.getDslPath());
        // 解析DSL
        var dsl = this.parseDsl(dslText);
        var project = Project.Builder.aReference()
                .workflowName(dsl.getFlow().getName())
                .workflowRef(dsl.getFlow().getRef())
                .dslText(dslText)
                .steps(dsl.getSteps())
                .gitRepoId(gitRepo.getId())
                .dslSource(Project.DslSource.GIT)
                .lastModifiedBy("admin")
                .build();
        // 创建流程
        var workflow = this.createWorkflow(dsl, project.getWorkflowVersion());
        // 保存原始DSL
        var dslSource = this.createDslSourceCode(project.getId(), workflow, dslText);
        // 将parameterMap转变为 InputParameter与Parameter参数Map
        var inputParameterMap = dsl.getInputParameterMap(project.getId(), project.getWorkflowVersion());
        // 返回DSL定义中的任务输入输出参数引用关系 ParameterRefer
        var parameterRefers = dsl.getFlow()
                .getParameterRefers(project.getWorkflowVersion());
        // 创建触发器
        if (null != dsl.getCron()) {
            var trigger = CronTrigger.Builder.aCronTrigger()
                    .projectId(project.getId())
                    .corn(dsl.getCron())
                    .build();
            this.cronTriggerRepository.add(trigger);
            this.scheduleJobService.addTrigger(trigger.getId(), trigger.getCorn());
        }
        this.projectRepository.add(project);
        this.gitRepoRepository.add(gitRepo);
        this.inputParameterRepository.addAll(new ArrayList<>(inputParameterMap.keySet()));
        this.jgitService.cleanUp(gitRepo.getId());
        this.parameterRepository.addAll(new ArrayList<>(inputParameterMap.values()));
        this.dslSourceCodeRepository.add(dslSource);
        this.workflowRepository.add(workflow);
        this.parameterReferRepository.addAll(parameterRefers);
    }

    @Transactional
    public void syncProject(String projectId) {
        logger.info("开始同步Git仓库中的DSL");
        var project = this.projectRepository.findById(projectId)
                .orElseThrow(() -> new DataNotFoundException("未找到该项目"));
        var gitRepo = this.gitRepoRepository.findById(project.getGitRepoId())
                .orElseThrow(() -> new DataNotFoundException("未找到Git仓库配置"));
        var triggers = this.cronTriggerRepository.findByProjectId(projectId);
        var dslText = this.jgitService.readDsl(gitRepo.getId(), gitRepo.getDslPath());
        // 解析DSL
        var dsl = this.parseDsl(dslText);
        project.setDslText(dslText);
        project.setLastModifiedBy("admin");
        project.setSteps(dsl.getSteps());
        project.setWorkflowName(dsl.getFlow().getName());
        project.setLastModifiedTime();
        project.setWorkflowVersion();
        // 创建流程
        var workflow = this.createWorkflow(dsl, project.getWorkflowVersion());
        // 保存原始DSL
        var dslSource = this.createDslSourceCode(project.getId(), workflow, dslText);
        // 将parameterMap转变为 InputParameter与Parameter参数Map
        var inputParameterMap = dsl.getInputParameterMap(project.getId(), project.getWorkflowVersion());
        // 返回DSL定义中的任务输入输出参数引用关系 ParameterRefer
        var parameterRefers = dsl.getFlow()
                .getParameterRefers(project.getWorkflowVersion());
        // 删除原有触发器
        this.cronTriggerRepository.deleteByProjectId(project.getId());
        triggers.forEach(cronTrigger -> {
            this.scheduleJobService.deleteTrigger(cronTrigger.getId());
        });
        // 创建新触发器
        if (null != dsl.getCron()) {
            var newTrigger = CronTrigger.Builder.aCronTrigger()
                    .projectId(projectId)
                    .corn(dsl.getCron())
                    .build();
            this.cronTriggerRepository.add(newTrigger);
            this.scheduleJobService.addTrigger(newTrigger.getId(), newTrigger.getCorn());
        }
        this.projectRepository.updateByWorkflowRef(project);
        this.dslSourceCodeRepository.add(dslSource);
        this.workflowRepository.add(workflow);
        this.parameterRepository.addAll(new ArrayList<>(inputParameterMap.values()));
        this.inputParameterRepository.addAll(new ArrayList<>(inputParameterMap.keySet()));
        this.parameterReferRepository.addAll(parameterRefers);
        this.jgitService.cleanUp(gitRepo.getId());
    }

    @Transactional
    public void createProject(String dslText) {
        // 解析DSL
        var dsl = this.parseDsl(dslText);
        // 创建项目
        var project = Project.Builder.aReference()
                .workflowName(dsl.getFlow().getName())
                .workflowRef(dsl.getFlow().getRef())
                .dslText(dslText)
                .steps(dsl.getSteps())
                .lastModifiedBy("admin")
                .gitRepoId("")
                .dslSource(Project.DslSource.LOCAL)
                .build();
        // 创建流程
        var workflow = this.createWorkflow(dsl, project.getWorkflowVersion());
        // 保存原始DSL
        var dslSource = this.createDslSourceCode(project.getId(), workflow, dslText);
        // 将parameterMap转变为 InputParameter与Parameter参数Map
        var inputParameterMap = dsl.getInputParameterMap(project.getId(), project.getWorkflowVersion());
        // 返回DSL定义中的任务输入输出参数引用关系 ParameterRefer
        var parameterRefers = dsl.getFlow()
                .getParameterRefers(project.getWorkflowVersion());
        // 创建触发器
        if (null != dsl.getCron()) {
            var trigger = CronTrigger.Builder.aCronTrigger()
                    .projectId(project.getId())
                    .corn(dsl.getCron())
                    .build();
            this.cronTriggerRepository.add(trigger);
            this.scheduleJobService.addTrigger(trigger.getId(), trigger.getCorn());
        }
        this.projectRepository.add(project);
        this.inputParameterRepository.addAll(new ArrayList<>(inputParameterMap.keySet()));
        this.parameterRepository.addAll(new ArrayList<>(inputParameterMap.values()));
        this.dslSourceCodeRepository.add(dslSource);
        this.workflowRepository.add(workflow);
        this.parameterReferRepository.addAll(parameterRefers);
    }

    @Transactional
    public void updateProject(String dslId, String dslText) {
        Project project = this.projectRepository.findById(dslId)
                .orElseThrow(() -> new DataNotFoundException("未找到该DSL"));
        var triggers = this.cronTriggerRepository.findByProjectId(project.getId());
        // 解析DSL
        var dsl = this.parseDsl(dslText);
        project.setDslText(dslText);
        project.setLastModifiedBy("admin");
        project.setSteps(dsl.getSteps());
        project.setWorkflowName(dsl.getFlow().getName());
        project.setLastModifiedTime();
        project.setWorkflowVersion();
        // 创建流程
        var workflow = this.createWorkflow(dsl, project.getWorkflowVersion());
        // 保存原始DSL
        var dslSource = this.createDslSourceCode(project.getId(), workflow, dslText);
        // 将parameterMap转变为 InputParameter与Parameter参数Map
        var inputParameterMap = dsl.getInputParameterMap(project.getId(), project.getWorkflowVersion());
        // 返回DSL定义中的任务输入输出参数引用关系 ParameterRefer
        var parameterRefers = dsl.getFlow()
                .getParameterRefers(project.getWorkflowVersion());
        // 删除原有触发器
        this.cronTriggerRepository.deleteByProjectId(project.getId());
        triggers.forEach(cronTrigger -> {
            this.scheduleJobService.deleteTrigger(cronTrigger.getId());
        });
        // 创建新触发器
        if (null != dsl.getCron()) {
            var newTrigger = CronTrigger.Builder.aCronTrigger()
                    .projectId(project.getId())
                    .corn(dsl.getCron())
                    .build();
            this.cronTriggerRepository.add(newTrigger);
            this.scheduleJobService.addTrigger(newTrigger.getId(), newTrigger.getCorn());
        }
        this.dslSourceCodeRepository.add(dslSource);
        this.projectRepository.updateByWorkflowRef(project);
        this.workflowRepository.add(workflow);
        this.parameterRepository.addAll(new ArrayList<>(inputParameterMap.values()));
        this.inputParameterRepository.addAll(new ArrayList<>(inputParameterMap.keySet()));
        this.parameterReferRepository.addAll(parameterRefers);
    }

    @Transactional
    public void deleteById(String id) {
        Project project = this.projectRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("未找到该DSL"));
        this.projectRepository.deleteByWorkflowRef(project.getWorkflowRef());
        this.workflowRepository.deleteByRef(project.getWorkflowRef());
        this.dslSourceCodeRepository.deleteByProjectId(project.getId());
        this.cronTriggerRepository.deleteByProjectId(project.getId());
        this.parameterReferRepository.deleteByWorkflowRef(project.getWorkflowRef());
        this.inputParameterRepository.deleteByProjectId(project.getId());
        this.gitRepoRepository.deleteById(project.getGitRepoId());
    }

    public PageInfo<Project> findAll(String workflowName, int pageNum, int pageSize) {
        return this.projectRepository.findAll(workflowName, pageNum, pageSize);
    }

    public Optional<Project> findById(String dslId) {
        return this.projectRepository.findById(dslId);
    }

    public DslSourceCode findByRefAndVersion(String ref, String version) {
        return this.dslSourceCodeRepository.findByRefAndVersion(ref, version).orElseThrow(() -> new DataNotFoundException("未找到该DSL"));
    }

    public GitRepo findGitRepoById(String gitRepoId) {
        return this.gitRepoRepository.findById(gitRepoId).orElseThrow(() -> new DataNotFoundException("未找到该Git库"));
    }
}
