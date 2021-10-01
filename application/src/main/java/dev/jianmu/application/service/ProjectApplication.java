package dev.jianmu.application.service;

import dev.jianmu.application.dsl.DslParser;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.query.NodeDef;
import dev.jianmu.application.query.NodeDefApi;
import dev.jianmu.infrastructure.jgit.JgitService;
import dev.jianmu.infrastructure.mybatis.project.ProjectRepositoryImpl;
import dev.jianmu.project.aggregate.CronTrigger;
import dev.jianmu.project.aggregate.GitRepo;
import dev.jianmu.project.aggregate.Project;
import dev.jianmu.project.event.CreatedEvent;
import dev.jianmu.project.event.DeletedEvent;
import dev.jianmu.project.event.TriggerEvent;
import dev.jianmu.project.repository.CronTriggerRepository;
import dev.jianmu.project.repository.GitRepoRepository;
import dev.jianmu.task.repository.TaskInstanceRepository;
import dev.jianmu.trigger.service.ScheduleJobService;
import dev.jianmu.workflow.aggregate.definition.GlobalParameter;
import dev.jianmu.workflow.aggregate.definition.Node;
import dev.jianmu.workflow.aggregate.definition.Workflow;
import dev.jianmu.workflow.repository.WorkflowInstanceRepository;
import dev.jianmu.workflow.repository.WorkflowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final CronTriggerRepository cronTriggerRepository;
    private final ScheduleJobService scheduleJobService;
    private final GitRepoRepository gitRepoRepository;
    private final WorkflowRepository workflowRepository;
    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final TaskInstanceRepository taskInstanceRepository;
    private final NodeDefApi nodeDefApi;
    private final ApplicationEventPublisher publisher;
    private final JgitService jgitService;

    public ProjectApplication(
            ProjectRepositoryImpl projectRepository,
            CronTriggerRepository cronTriggerRepository,
            ScheduleJobService scheduleJobService,
            GitRepoRepository gitRepoRepository,
            WorkflowRepository workflowRepository,
            WorkflowInstanceRepository workflowInstanceRepository,
            TaskInstanceRepository taskInstanceRepository,
            NodeDefApi nodeDefApi,
            ApplicationEventPublisher publisher,
            JgitService jgitService
    ) {
        this.projectRepository = projectRepository;
        this.cronTriggerRepository = cronTriggerRepository;
        this.scheduleJobService = scheduleJobService;
        this.gitRepoRepository = gitRepoRepository;
        this.workflowRepository = workflowRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.taskInstanceRepository = taskInstanceRepository;
        this.nodeDefApi = nodeDefApi;
        this.publisher = publisher;
        this.jgitService = jgitService;
    }

    public void trigger(String projectId, String triggerId) {
        MDC.put("triggerId", triggerId);
        var project = this.projectRepository.findById(projectId)
                .orElseThrow(() -> new DataNotFoundException("未找到该项目"));
        var triggerEvent = TriggerEvent.Builder.aTriggerEvent()
                .projectId(project.getId())
                .triggerId(triggerId)
                .workflowRef(project.getWorkflowRef())
                .workflowVersion(project.getWorkflowVersion())
                .build();
        this.publisher.publishEvent(triggerEvent);
    }

    public void trigger(String projectId) {
        var project = this.projectRepository.findById(projectId)
                .orElseThrow(() -> new DataNotFoundException("未找到该项目"));
        var triggerId = UUID.randomUUID().toString().replace("-", "");
        MDC.put("triggerId", triggerId);
        var triggerEvent = TriggerEvent.Builder.aTriggerEvent()
                .projectId(project.getId())
                .triggerId(triggerId)
                .workflowRef(project.getWorkflowRef())
                .workflowVersion(project.getWorkflowVersion())
                .build();
        this.publisher.publishEvent(triggerEvent);
    }

    public void triggerFromCron(String triggerId) {
        MDC.put("triggerId", triggerId);
        var cronTrigger = this.cronTriggerRepository.findById(triggerId)
                .orElseThrow(() -> new DataNotFoundException("未找到该触发器"));
        this.trigger(cronTrigger.getProjectId(), triggerId);
    }

    private Workflow createWorkflow(DslParser parser, String dslText) {
        // 查询相关的节点定义
        var types = parser.getAsyncTaskTypes();
        var nodeDefs = this.nodeDefApi.findByTypes(types);

        // 根据节点定义与DSL节点列表创建Workflow
        var nodes = parser.createNodes(nodeDefs);
        Set<GlobalParameter> globalParameters = Set.of();
        if (parser.getParam() != null) {
            globalParameters = Workflow.createGlobalParameters(parser.getParam());
        }
        return Workflow.Builder.aWorkflow()
                .ref(parser.getRef())
                .type(parser.getType())
                .name(parser.getName())
                .description(parser.getDescription())
                .nodes(nodes)
                .globalParameters(globalParameters)
                .dslText(dslText)
                .build();
    }

    @Transactional
    public void importProject(GitRepo gitRepo) {
        var dslText = this.jgitService.readDsl(gitRepo.getId(), gitRepo.getDslPath());
        // 解析DSL,语法检查
        var parser = DslParser.parse(dslText);
        var workflow = this.createWorkflow(parser, dslText);
        var project = Project.Builder.aReference()
                .workflowName(parser.getName())
                .workflowRef(parser.getRef())
                .workflowVersion(workflow.getVersion())
                .dslText(dslText)
                .steps(parser.getSteps())
                .gitRepoId(gitRepo.getId())
                .dslSource(Project.DslSource.GIT)
                .triggerType(Project.TriggerType.MANUAL)
                .dslType(parser.getType().equals(Workflow.Type.WORKFLOW) ? Project.DslType.WORKFLOW : Project.DslType.PIPELINE)
                .lastModifiedBy("admin")
                .build();
        // 创建触发器
        if (null != parser.getCron()) {
            var trigger = CronTrigger.Builder.aCronTrigger()
                    .projectId(project.getId())
                    .corn(parser.getCron())
                    .build();
            this.cronTriggerRepository.add(trigger);
            this.scheduleJobService.addTrigger(trigger.getId(), trigger.getCorn());
        }
        this.projectRepository.add(project);
        this.gitRepoRepository.add(gitRepo);
        this.jgitService.cleanUp(gitRepo.getId());
        this.workflowRepository.add(workflow);
        this.publisher.publishEvent(new CreatedEvent(project.getId()));
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
        // 解析DSL,语法检查
        var parser = DslParser.parse(dslText);
        var workflow = this.createWorkflow(parser, dslText);
        project.setDslText(dslText);
        project.setDslType(parser.getType().equals(Workflow.Type.WORKFLOW) ? Project.DslType.WORKFLOW : Project.DslType.PIPELINE);
        project.setLastModifiedBy("admin");
        project.setSteps(parser.getSteps());
        project.setWorkflowName(parser.getName());
        project.setLastModifiedTime();
        project.setWorkflowVersion(workflow.getVersion());
        // 删除原有触发器
        this.cronTriggerRepository.deleteByProjectId(project.getId());
        triggers.forEach(cronTrigger -> {
            this.scheduleJobService.deleteTrigger(cronTrigger.getId());
        });
        // 创建新触发器
        if (null != parser.getCron()) {
            var newTrigger = CronTrigger.Builder.aCronTrigger()
                    .projectId(projectId)
                    .corn(parser.getCron())
                    .build();
            this.cronTriggerRepository.add(newTrigger);
            this.scheduleJobService.addTrigger(newTrigger.getId(), newTrigger.getCorn());
        }
        this.projectRepository.updateByWorkflowRef(project);
        this.workflowRepository.add(workflow);
        this.jgitService.cleanUp(gitRepo.getId());
    }

    @Transactional
    public void createProject(String dslText) {
        // 解析DSL,语法检查
        var parser = DslParser.parse(dslText);
        var workflow = this.createWorkflow(parser, dslText);
        // 创建项目
        var project = Project.Builder.aReference()
                .workflowName(workflow.getName())
                .workflowRef(workflow.getRef())
                .workflowVersion(workflow.getVersion())
                .dslText(dslText)
                .steps(parser.getSteps())
                .lastModifiedBy("admin")
                .gitRepoId("")
                .dslSource(Project.DslSource.LOCAL)
                .triggerType(Project.TriggerType.MANUAL)
                .dslType(parser.getType().equals(Workflow.Type.WORKFLOW) ? Project.DslType.WORKFLOW : Project.DslType.PIPELINE)
                .build();
        // 创建触发器
        if (null != parser.getCron()) {
            var trigger = CronTrigger.Builder.aCronTrigger()
                    .projectId(project.getId())
                    .corn(parser.getCron())
                    .build();
            this.cronTriggerRepository.add(trigger);
            this.scheduleJobService.addTrigger(trigger.getId(), trigger.getCorn());
        }
        this.projectRepository.add(project);
        this.workflowRepository.add(workflow);
        this.publisher.publishEvent(new CreatedEvent(project.getId()));
    }

    @Transactional
    public void updateProject(String dslId, String dslText) {
        Project project = this.projectRepository.findById(dslId)
                .orElseThrow(() -> new DataNotFoundException("未找到该DSL"));
        var triggers = this.cronTriggerRepository.findByProjectId(project.getId());
        // 解析DSL,语法检查
        var parser = DslParser.parse(dslText);
        if (!parser.getRef().equals(project.getWorkflowRef())) {
            throw new RuntimeException("ref不一致");
        }
        var workflow = this.createWorkflow(parser, dslText);
        project.setDslText(dslText);
        project.setDslType(parser.getType().equals(Workflow.Type.WORKFLOW) ? Project.DslType.WORKFLOW : Project.DslType.PIPELINE);
        project.setLastModifiedBy("admin");
        project.setSteps(parser.getSteps());
        project.setWorkflowName(parser.getName());
        project.setLastModifiedTime();
        project.setWorkflowVersion(workflow.getVersion());
        // 删除原有触发器
        this.cronTriggerRepository.deleteByProjectId(project.getId());
        triggers.forEach(cronTrigger -> {
            this.scheduleJobService.deleteTrigger(cronTrigger.getId());
        });
        // 创建新触发器
        if (null != parser.getCron()) {
            var newTrigger = CronTrigger.Builder.aCronTrigger()
                    .projectId(project.getId())
                    .corn(parser.getCron())
                    .build();
            this.cronTriggerRepository.add(newTrigger);
            this.scheduleJobService.addTrigger(newTrigger.getId(), newTrigger.getCorn());
        }
        this.projectRepository.updateByWorkflowRef(project);
        this.workflowRepository.add(workflow);
    }

    @Transactional
    public void deleteById(String id) {
        Project project = this.projectRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("未找到该DSL"));
        this.projectRepository.deleteByWorkflowRef(project.getWorkflowRef());
        this.workflowRepository.deleteByRef(project.getWorkflowRef());
        this.workflowInstanceRepository.deleteByWorkflowRef(project.getWorkflowRef());
        this.taskInstanceRepository.deleteByWorkflowRef(project.getWorkflowRef());
        this.cronTriggerRepository.deleteByProjectId(project.getId());
        this.gitRepoRepository.deleteById(project.getGitRepoId());
        this.publisher.publishEvent(new DeletedEvent(project.getId()));
    }

    public List<Project> findAll() {
        return this.projectRepository.findAll();
    }

    public Optional<Project> findById(String dslId) {
        return this.projectRepository.findById(dslId);
    }

    public List<NodeDef> findNodes(String ref, String version) {
        var workflow = this.workflowRepository.findByRefAndVersion(ref, version)
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义"));
        var types = workflow.findTasks().stream()
                .map(Node::getType)
                .collect(Collectors.toSet());
        return this.nodeDefApi.findByTypes(types);
    }

    public Workflow findByRefAndVersion(String ref, String version) {
        return this.workflowRepository.findByRefAndVersion(ref, version)
                .orElseThrow(() -> new DataNotFoundException("未找到该Workflow"));
    }

    public GitRepo findGitRepoById(String gitRepoId) {
        return this.gitRepoRepository.findById(gitRepoId).orElseThrow(() -> new DataNotFoundException("未找到该Git库"));
    }

    public String getNextFireTime(String projectId) {
        var cronTriggers = this.cronTriggerRepository.findByProjectId(projectId);
        if (!cronTriggers.isEmpty()) {
            var c = cronTriggers.get(0);
            return this.scheduleJobService.getNextFireTime(c.getId());
        }
        return "";
    }
}
