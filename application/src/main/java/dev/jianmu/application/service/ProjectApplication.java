package dev.jianmu.application.service;

import com.github.pagehelper.PageInfo;
import dev.jianmu.application.dsl.DslParser;
import dev.jianmu.application.event.CronEvent;
import dev.jianmu.application.event.ManualEvent;
import dev.jianmu.application.event.WebhookEvent;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.query.NodeDefApi;
import dev.jianmu.infrastructure.jgit.JgitService;
import dev.jianmu.infrastructure.mybatis.project.ProjectRepositoryImpl;
import dev.jianmu.project.aggregate.GitRepo;
import dev.jianmu.project.aggregate.Project;
import dev.jianmu.project.aggregate.ProjectGroup;
import dev.jianmu.project.aggregate.ProjectLinkGroup;
import dev.jianmu.project.event.CreatedEvent;
import dev.jianmu.project.event.DeletedEvent;
import dev.jianmu.project.event.TriggerEvent;
import dev.jianmu.project.repository.GitRepoRepository;
import dev.jianmu.project.repository.ProjectGroupRepository;
import dev.jianmu.project.repository.ProjectLinkGroupRepository;
import dev.jianmu.task.repository.TaskInstanceRepository;
import dev.jianmu.trigger.aggregate.Webhook;
import dev.jianmu.workflow.aggregate.definition.Workflow;
import dev.jianmu.workflow.aggregate.process.ProcessStatus;
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
import java.util.UUID;

import static dev.jianmu.application.service.ProjectGroupApplication.DEFAULT_PROJECT_GROUP_NAME;

/**
 * @author Ethan Liu
 * @class ProjectApplication
 * @description ProjectApplication
 * @create 2021-05-15 22:13
 */
@Service
public class ProjectApplication {
    private static final Logger logger = LoggerFactory.getLogger(ProjectApplication.class);

    private final ProjectRepositoryImpl projectRepository;
    private final GitRepoRepository gitRepoRepository;
    private final WorkflowRepository workflowRepository;
    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final TaskInstanceRepository taskInstanceRepository;
    private final NodeDefApi nodeDefApi;
    private final ApplicationEventPublisher publisher;
    private final JgitService jgitService;
    private final ProjectLinkGroupRepository projectLinkGroupRepository;
    private final ProjectGroupRepository projectGroupRepository;

    public ProjectApplication(
            ProjectRepositoryImpl projectRepository,
            GitRepoRepository gitRepoRepository,
            WorkflowRepository workflowRepository,
            WorkflowInstanceRepository workflowInstanceRepository,
            TaskInstanceRepository taskInstanceRepository,
            NodeDefApi nodeDefApi,
            ApplicationEventPublisher publisher,
            JgitService jgitService,
            ProjectLinkGroupRepository projectLinkGroupRepository, ProjectGroupRepository projectGroupRepository) {
        this.projectRepository = projectRepository;
        this.gitRepoRepository = gitRepoRepository;
        this.workflowRepository = workflowRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.taskInstanceRepository = taskInstanceRepository;
        this.nodeDefApi = nodeDefApi;
        this.publisher = publisher;
        this.jgitService = jgitService;
        this.projectLinkGroupRepository = projectLinkGroupRepository;
        this.projectGroupRepository = projectGroupRepository;
    }

    public void trigger(String projectId, String triggerId, String triggerType) {
        MDC.put("triggerId", triggerId);
        var project = this.projectRepository.findById(projectId)
                .orElseThrow(() -> new DataNotFoundException("未找到该项目"));
        var triggerEvent = TriggerEvent.Builder.aTriggerEvent()
                .projectId(project.getId())
                .triggerId(triggerId)
                .triggerType(triggerType)
                .workflowRef(project.getWorkflowRef())
                .workflowVersion(project.getWorkflowVersion())
                .build();
        this.publisher.publishEvent(triggerEvent);
    }

    public void triggerByManual(String projectId) {
        var project = this.projectRepository.findById(projectId)
                .orElseThrow(() -> new DataNotFoundException("未找到该项目"));
        var triggerId = UUID.randomUUID().toString().replace("-", "");
        MDC.put("triggerId", triggerId);
        var triggerEvent = TriggerEvent.Builder.aTriggerEvent()
                .projectId(project.getId())
                .triggerId(triggerId)
                .triggerType("MANUAL")
                .workflowRef(project.getWorkflowRef())
                .workflowVersion(project.getWorkflowVersion())
                .build();
        this.publisher.publishEvent(triggerEvent);
    }

    private Workflow createWorkflow(DslParser parser, String dslText, String ref) {
        // 保存Shell node定义
        var shellNodes = parser.getShellNodes();
        this.nodeDefApi.addShellNodes(shellNodes);

        // 查询相关的节点定义
        var types = parser.getAsyncTaskTypes();
        var nodeDefs = this.nodeDefApi.getByTypes(types);

        // 根据节点定义与DSL节点列表创建Workflow
        var nodes = parser.createNodes(nodeDefs);
        return Workflow.Builder.aWorkflow()
                .ref(ref)
                .type(parser.getType())
                .name(parser.getName())
                .description(parser.getDescription())
                .nodes(nodes)
                .globalParameters(parser.getGlobalParameters())
                .dslText(dslText)
                .build();
    }

    @Transactional
    public void importProject(GitRepo gitRepo, String projectGroupId) {
        var dslText = this.jgitService.readDsl(gitRepo.getId(), gitRepo.getDslPath());
        // 解析DSL,语法检查
        var parser = DslParser.parse(dslText);
        // 生成流程Ref
        var ref = UUID.randomUUID().toString().replace("-", "");
        var workflow = this.createWorkflow(parser, dslText, ref);
        var project = Project.Builder.aReference()
                .workflowName(parser.getName())
                .workflowDescription(parser.getDescription())
                .workflowRef(workflow.getRef())
                .workflowVersion(workflow.getVersion())
                .dslText(dslText)
                .steps(parser.getSteps())
                .gitRepoId(gitRepo.getId())
                .dslSource(Project.DslSource.GIT)
                .triggerType(parser.getTriggerType())
                .dslType(parser.getType().equals(Workflow.Type.WORKFLOW) ? Project.DslType.WORKFLOW : Project.DslType.PIPELINE)
                .lastModifiedBy("admin")
                .build();
        // 添加到默认分组
        String groupId;
        if (projectGroupId != null) {
            this.projectGroupRepository.findById(projectGroupId).orElseThrow(() -> new DataNotFoundException("未找到该项目组"));
            groupId = projectGroupId;
        }else {
            groupId = this.projectGroupRepository.findByName(DEFAULT_PROJECT_GROUP_NAME).map(ProjectGroup::getId)
                    .orElseThrow(() -> new DataNotFoundException("未找到默认项目组"));
        }
        var sort = this.projectLinkGroupRepository.findByProjectGroupIdAndSortMax(groupId)
                .map(ProjectLinkGroup::getSort)
                .orElse(-1);
        var projectLinkGroup = ProjectLinkGroup.Builder.aReference()
                .projectGroupId(groupId)
                .projectId(project.getId())
                .sort(++sort)
                .build();
        this.pubTriggerEvent(parser, project);
        this.projectRepository.add(project);
        this.projectLinkGroupRepository.add(projectLinkGroup);
        this.projectGroupRepository.addProjectCountById(groupId, 1);
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
        var dslText = this.jgitService.readDsl(gitRepo.getId(), gitRepo.getDslPath());
        // 解析DSL,语法检查
        var parser = DslParser.parse(dslText);
        var workflow = this.createWorkflow(parser, dslText, project.getWorkflowRef());
        project.setDslText(dslText);
        project.setDslType(parser.getType().equals(Workflow.Type.WORKFLOW) ? Project.DslType.WORKFLOW : Project.DslType.PIPELINE);
        project.setTriggerType(Project.TriggerType.MANUAL);
        project.setLastModifiedBy("admin");
        project.setSteps(parser.getSteps());
        project.setWorkflowName(parser.getName());
        project.setWorkflowDescription(parser.getDescription());
        project.setLastModifiedTime();
        project.setWorkflowVersion(workflow.getVersion());
        project.setTriggerType(parser.getTriggerType());

        this.pubTriggerEvent(parser, project);
        this.projectRepository.updateByWorkflowRef(project);
        this.workflowRepository.add(workflow);
        this.jgitService.cleanUp(gitRepo.getId());
    }

    @Transactional
    public void createProject(String dslText, String projectGroupId) {
        // 解析DSL,语法检查
        var parser = DslParser.parse(dslText);
        // 生成流程Ref
        var ref = UUID.randomUUID().toString().replace("-", "");
        var workflow = this.createWorkflow(parser, dslText, ref);
        // 创建项目
        var project = Project.Builder.aReference()
                .workflowName(workflow.getName())
                .workflowDescription(workflow.getDescription())
                .workflowRef(workflow.getRef())
                .workflowVersion(workflow.getVersion())
                .dslText(dslText)
                .steps(parser.getSteps())
                .lastModifiedBy("admin")
                .gitRepoId("")
                .dslSource(Project.DslSource.LOCAL)
                .triggerType(parser.getTriggerType())
                .dslType(parser.getType().equals(Workflow.Type.WORKFLOW) ? Project.DslType.WORKFLOW : Project.DslType.PIPELINE)
                .build();
        // 添加分组
        this.projectGroupRepository.findById(projectGroupId).orElseThrow(() -> new DataNotFoundException("未找到该项目组"));
        var sort = this.projectLinkGroupRepository.findByProjectGroupIdAndSortMax(projectGroupId)
                .map(ProjectLinkGroup::getSort)
                .orElse(-1);
        var projectLinkGroup = ProjectLinkGroup.Builder.aReference()
                .projectGroupId(projectGroupId)
                .projectId(project.getId())
                .sort(++sort)
                .build();

        this.pubTriggerEvent(parser, project);
        this.projectRepository.add(project);
        this.projectLinkGroupRepository.add(projectLinkGroup);
        this.projectGroupRepository.addProjectCountById(projectGroupId, 1);
        this.workflowRepository.add(workflow);
        this.publisher.publishEvent(new CreatedEvent(project.getId()));
    }

    @Transactional
    public void updateProject(String dslId, String dslText, String projectGroupId) {
        Project project = this.projectRepository.findById(dslId)
                .orElseThrow(() -> new DataNotFoundException("未找到该DSL"));
        if (project.getDslSource() == Project.DslSource.GIT) {
            throw new IllegalArgumentException("不能修改通过Git导入的项目");
        }
        // 解析DSL,语法检查
        var parser = DslParser.parse(dslText);
        var workflow = this.createWorkflow(parser, dslText, project.getWorkflowRef());
        project.setDslText(dslText);
        project.setDslType(parser.getType().equals(Workflow.Type.WORKFLOW) ? Project.DslType.WORKFLOW : Project.DslType.PIPELINE);
        project.setTriggerType(parser.getTriggerType());
        project.setLastModifiedBy("admin");
        project.setSteps(parser.getSteps());
        project.setWorkflowName(parser.getName());
        project.setWorkflowDescription(parser.getDescription());
        project.setLastModifiedTime();
        project.setWorkflowVersion(workflow.getVersion());
        // 修改项目组
        this.updateProjectGroup(dslId, projectGroupId);

        this.pubTriggerEvent(parser, project);
        this.projectRepository.updateByWorkflowRef(project);
        this.workflowRepository.add(workflow);
    }

    private void updateProjectGroup(String dslId, String projectGroupId) {
        var projectLinkGroup = this.projectLinkGroupRepository.findByProjectId(dslId)
                .orElseThrow(() -> new DataNotFoundException("未找到该项目关联项目组"));
        var targetGroupId = this.projectGroupRepository.findById(projectGroupId).map(ProjectGroup::getId)
                .orElseThrow(() -> new DataNotFoundException("未找到目标项目组"));
        this.projectLinkGroupRepository.deleteById(projectLinkGroup.getId());
        var sort = this.projectLinkGroupRepository.findByProjectGroupIdAndSortMax(targetGroupId)
                .map(ProjectLinkGroup::getSort)
                .orElse(-1);
        var newProjectLinkGroup = ProjectLinkGroup.Builder.aReference()
                .projectGroupId(targetGroupId)
                .projectId(projectLinkGroup.getProjectId())
                .sort(++sort)
                .build();
        this.projectLinkGroupRepository.add(newProjectLinkGroup);
        this.projectGroupRepository.subProjectCountById(projectLinkGroup.getProjectGroupId(), 1);
        this.projectGroupRepository.addProjectCountById(targetGroupId, 1);
    }

    @Transactional
    public void deleteById(String id) {
        Project project = this.projectRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("未找到该项目"));
        var running = this.workflowInstanceRepository
                .findByRefAndVersionAndStatus(project.getWorkflowRef(), project.getWorkflowVersion(), ProcessStatus.RUNNING)
                .size();
        if (running > 0) {
            throw new RuntimeException("仍有流程执行中，不能删除");
        }
        var projectLinkGroup = this.projectLinkGroupRepository.findByProjectId(id)
                .orElseThrow(() -> new DataNotFoundException("未找到项目分组"));
        this.projectLinkGroupRepository.deleteById(projectLinkGroup.getId());
        this.projectGroupRepository.subProjectCountById(projectLinkGroup.getProjectGroupId(), 1);
        this.projectRepository.deleteByWorkflowRef(project.getWorkflowRef());
        this.workflowRepository.deleteByRef(project.getWorkflowRef());
        this.workflowInstanceRepository.deleteByWorkflowRef(project.getWorkflowRef());
        this.taskInstanceRepository.deleteByWorkflowRef(project.getWorkflowRef());
        this.gitRepoRepository.deleteById(project.getGitRepoId());
        this.publisher.publishEvent(new DeletedEvent(project.getId()));
    }

    private void pubTriggerEvent(DslParser parser, Project project) {
        // 创建Cron触发器
        if (project.getTriggerType() == Project.TriggerType.CRON) {
            var cronEvent = CronEvent.builder()
                    .projectId(project.getId())
                    .schedule(parser.getCron())
                    .build();
            this.publisher.publishEvent(cronEvent);
        }
        // 创建Webhook触发器
        if (project.getTriggerType() == Project.TriggerType.WEBHOOK) {
            var webhook = Webhook.Builder.aWebhook()
                    .only(parser.getWebhook().getOnly())
                    .auth(parser.getWebhook().getAuth())
                    .param(parser.getWebhook().getParam())
                    .build();
            var webhookEvent = WebhookEvent.builder()
                    .projectId(project.getId())
                    .webhook(webhook)
                    .build();
            this.publisher.publishEvent(webhookEvent);
        }
        // 当触发类型为手动时
        if (project.getTriggerType() == Project.TriggerType.MANUAL) {
            var manualEvent = ManualEvent.builder()
                    .projectId(project.getId())
                    .build();
            this.publisher.publishEvent(manualEvent);
        }
    }

    public List<Project> findAll() {
        return this.projectRepository.findAll();
    }

    public Optional<Project> findById(String dslId) {
        return this.projectRepository.findById(dslId);
    }

    public Workflow findByRefAndVersion(String ref, String version) {
        return this.workflowRepository.findByRefAndVersion(ref, version)
                .orElseThrow(() -> new DataNotFoundException("未找到该Workflow"));
    }

    public GitRepo findGitRepoById(String gitRepoId) {
        return this.gitRepoRepository.findById(gitRepoId).orElseThrow(() -> new DataNotFoundException("未找到该Git库"));
    }

    public PageInfo<Project> findPageByGroupId(Integer pageNum, Integer pageSize, String projectGroupId, String workflowName) {
        return this.projectRepository.findPageByGroupId(pageNum, pageSize, projectGroupId, workflowName);
    }
}
