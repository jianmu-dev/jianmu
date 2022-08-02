package dev.jianmu.application.service;

import com.github.pagehelper.PageInfo;
import dev.jianmu.application.dsl.DslParser;
import dev.jianmu.application.event.CronEvent;
import dev.jianmu.application.event.ManualEvent;
import dev.jianmu.application.event.WebhookEvent;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.exception.NoAssociatedPermissionException;
import dev.jianmu.application.query.NodeDefApi;
import dev.jianmu.application.util.AssociationUtil;
import dev.jianmu.git.repo.aggregate.Flow;
import dev.jianmu.git.repo.repository.GitRepoRepository;
import dev.jianmu.infrastructure.GlobalProperties;
import dev.jianmu.infrastructure.mybatis.project.ProjectRepositoryImpl;
import dev.jianmu.oauth2.api.config.OAuth2Properties;
import dev.jianmu.oauth2.api.enumeration.ThirdPartyTypeEnum;
import dev.jianmu.oauth2.api.exception.UnknownException;
import dev.jianmu.oauth2.api.impl.OAuth2ApiProxy;
import dev.jianmu.oauth2.api.util.AESEncryptionUtil;
import dev.jianmu.project.aggregate.Project;
import dev.jianmu.project.aggregate.ProjectGroup;
import dev.jianmu.project.aggregate.ProjectLastExecution;
import dev.jianmu.project.aggregate.ProjectLinkGroup;
import dev.jianmu.project.event.CreatedEvent;
import dev.jianmu.project.event.DeletedEvent;
import dev.jianmu.project.event.MovedEvent;
import dev.jianmu.project.event.TriggerEvent;
import dev.jianmu.project.query.ProjectVo;
import dev.jianmu.project.repository.ProjectGroupRepository;
import dev.jianmu.project.repository.ProjectLastExecutionRepository;
import dev.jianmu.project.repository.ProjectLinkGroupRepository;
import dev.jianmu.task.repository.TaskInstanceRepository;
import dev.jianmu.trigger.aggregate.Trigger;
import dev.jianmu.trigger.aggregate.Webhook;
import dev.jianmu.trigger.repository.TriggerEventRepository;
import dev.jianmu.workflow.aggregate.definition.Workflow;
import dev.jianmu.workflow.aggregate.process.ProcessStatus;
import dev.jianmu.workflow.repository.AsyncTaskInstanceRepository;
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
    private static final String dslLocation = ".devops/";

    private final ProjectRepositoryImpl projectRepository;
    private final WorkflowRepository workflowRepository;
    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final AsyncTaskInstanceRepository asyncTaskInstanceRepository;
    private final TaskInstanceRepository taskInstanceRepository;
    private final NodeDefApi nodeDefApi;
    private final ApplicationEventPublisher publisher;
    private final ProjectLinkGroupRepository projectLinkGroupRepository;
    private final ProjectGroupRepository projectGroupRepository;
    private final GlobalProperties globalProperties;
    private final TriggerEventRepository triggerEventRepository;
    private final ProjectLastExecutionRepository projectLastExecutionRepository;
    private final GitRepoRepository gitRepoRepository;
    private final OAuth2Properties oAuth2Properties;

    public ProjectApplication(
            ProjectRepositoryImpl projectRepository,
            WorkflowRepository workflowRepository,
            WorkflowInstanceRepository workflowInstanceRepository,
            AsyncTaskInstanceRepository asyncTaskInstanceRepository,
            TaskInstanceRepository taskInstanceRepository,
            NodeDefApi nodeDefApi,
            ApplicationEventPublisher publisher,
            ProjectLinkGroupRepository projectLinkGroupRepository,
            ProjectGroupRepository projectGroupRepository,
            GlobalProperties globalProperties,
            TriggerEventRepository triggerEventRepository,
            ProjectLastExecutionRepository projectLastExecutionRepository,
            GitRepoRepository gitRepoRepository,
            OAuth2Properties oAuth2Properties
    ) {
        this.projectRepository = projectRepository;
        this.workflowRepository = workflowRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.asyncTaskInstanceRepository = asyncTaskInstanceRepository;
        this.taskInstanceRepository = taskInstanceRepository;
        this.nodeDefApi = nodeDefApi;
        this.publisher = publisher;
        this.projectLinkGroupRepository = projectLinkGroupRepository;
        this.projectGroupRepository = projectGroupRepository;
        this.globalProperties = globalProperties;
        this.triggerEventRepository = triggerEventRepository;
        this.projectLastExecutionRepository = projectLastExecutionRepository;
        this.gitRepoRepository = gitRepoRepository;
        this.oAuth2Properties = oAuth2Properties;
    }

    public void switchEnabled(String projectId, boolean enabled) {
        var project = this.projectRepository.findById(projectId)
                .orElseThrow(() -> new DataNotFoundException("未找到该项目"));
        project.switchEnabled(enabled);
        this.projectRepository.updateByWorkflowRef(project);
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

    public void triggerByManual(String projectId, String associationId, String associationType) {
        var project = this.projectRepository.findById(projectId)
                .orElseThrow(() -> new DataNotFoundException("未找到该项目"));
        this.checkProjectPermission(associationId, associationType, project);
        if (!project.isEnabled()) {
            throw new RuntimeException("当前项目不可触发，请先修改状态");
        }

        var evt = dev.jianmu.trigger.event.TriggerEvent.Builder
                .aTriggerEvent()
                .projectId(project.getId())
                .triggerId("")
                .triggerType(Trigger.Type.MANUAL.name())
                .build();
        this.triggerEventRepository.save(evt);

        MDC.put("triggerId", evt.getId());
        var triggerEvent = TriggerEvent.Builder.aTriggerEvent()
                .projectId(project.getId())
                .triggerId(evt.getId())
                .triggerType(Trigger.Type.MANUAL.name())
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
                .tag(parser.getTag())
                .name(parser.getName())
                .description(parser.getDescription())
                .nodes(nodes)
                .globalParameters(parser.getGlobalParameters())
                .dslText(dslText)
                .build();
    }

    @Transactional
    public Project createProject(String dslText, String projectGroupId, String username, String associationId, String associationType, String branch, String encryptedToken, String userId, boolean isSyncProject) {
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
                .concurrent(parser.isConcurrent())
                .lastModifiedBy(username)
                .gitRepoId("")
                .dslSource(Project.DslSource.LOCAL)
                .triggerType(parser.getTriggerType())
                .dslType(parser.getType().equals(Workflow.Type.WORKFLOW) ? Project.DslType.WORKFLOW : Project.DslType.PIPELINE)
                .associationId(associationId)
                .associationType(associationType)
                .build();
        // 添加分组
        if (projectGroupId == null) {
            projectGroupId = this.projectGroupRepository.findByName(DEFAULT_PROJECT_GROUP_NAME).map(ProjectGroup::getId)
                    .orElseThrow(() -> new DataNotFoundException("未找到默认项目组"));
        }else{
            this.projectGroupRepository.findById(projectGroupId).orElseThrow(() -> new DataNotFoundException("未找到该项目组"));
        }
        var sort = this.projectLinkGroupRepository.findByProjectGroupIdAndSortMax(projectGroupId)
                .map(ProjectLinkGroup::getSort)
                .orElse(-1);
        var projectLinkGroup = ProjectLinkGroup.Builder.aReference()
                .projectGroupId(projectGroupId)
                .projectId(project.getId())
                .sort(++sort)
                .build();

        this.pubTriggerEvent(parser, project, userId, encryptedToken);
        if (isSyncProject) {
            this.createOrUpdateGitFile(branch, encryptedToken, project, project.getWorkflowName(), userId);
        }
        this.projectRepository.add(project);
        this.projectLastExecutionRepository.add(new ProjectLastExecution(project.getWorkflowRef()));
        this.projectLinkGroupRepository.add(projectLinkGroup);
        this.projectGroupRepository.addProjectCountById(projectGroupId, 1);
        this.workflowRepository.add(workflow);
        this.publisher.publishEvent(new CreatedEvent(project.getId(), branch, associationId));
        return project;
    }

    private void createOrUpdateGitFile(String branch, String encryptedToken, Project project, String oldName, String userId) {
        if (!AssociationUtil.AssociationType.GIT_REPO.name().equals(project.getAssociationType())) {
            return;
        }
        var gitRepo = this.gitRepoRepository.findById(project.getAssociationId())
                .orElseThrow(() -> new DataNotFoundException("未找到Git仓库"));
        if (branch == null)
            branch = gitRepo.findFlowByProjectId(project.getId())
                    .map(Flow::getBranchName)
                    .orElseThrow(() -> new DataNotFoundException("未找到Git仓库项目"));
        var dsl = project.getDslText().replaceAll("\nraw-data: \"\\{.*}\"$", "");
        var filepath = dslLocation + project.getWorkflowName() + ".yml";
        try {
            var oAuth2Api = OAuth2ApiProxy.builder()
                    .thirdPartyType(ThirdPartyTypeEnum.valueOf(this.oAuth2Properties.getThirdPartyType()))
                    .userId(userId)
                    .build();
            var accessToken = AESEncryptionUtil.decrypt(encryptedToken, this.oAuth2Properties.getClientSecret());
            try {
                if (!project.getWorkflowName().equals(oldName)) {
                    var oldFilepath = dslLocation + oldName + ".yml";
                    oAuth2Api.deleteFile(accessToken, gitRepo.getOwner(), gitRepo.getRef(), "", oldFilepath, null, null, null, null, branch, "refactor: delete " + oldFilepath);
                }
                oAuth2Api.getFile(accessToken, gitRepo.getOwner(), gitRepo.getRef(), filepath, branch);
                oAuth2Api.updateFile(accessToken, gitRepo.getOwner(), gitRepo.getRef(), dsl, filepath, null, null, null, null, branch, "refactor: " + filepath);
            } catch (UnknownException e) {
                oAuth2Api.createFile(accessToken, gitRepo.getOwner(), gitRepo.getRef(), dsl, filepath, null, null, null, null, branch, "feat: " + filepath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("创建或修改项目失败: " + e.getMessage());
        }
    }

    @Transactional
    public boolean updateProject(String dslId, String dslText, String projectGroupId, String username, String associationId, String associationType, String encryptedToken, String userId, boolean isSyncProject) {
        Project project = this.projectRepository.findById(dslId)
                .orElseThrow(() -> new DataNotFoundException("未找到该DSL"));
        if (username != null) {
            this.checkProjectPermission(associationId, associationType, project);
        }
        var concurrent = project.isConcurrent();
        var oldName = project.getWorkflowName();
        // 移动项目到项目组
        this.publisher.publishEvent(new MovedEvent(project.getId(), projectGroupId));
        // 解析DSL,语法检查
        var parser = DslParser.parse(dslText);
        var workflow = this.createWorkflow(parser, dslText, project.getWorkflowRef());
        if (project.getDslText().equals(dslText)) {
            return false;
        }
        project.setDslText(dslText);
        project.setDslType(parser.getType().equals(Workflow.Type.WORKFLOW) ? Project.DslType.WORKFLOW : Project.DslType.PIPELINE);
        project.setTriggerType(parser.getTriggerType());
        project.setSteps(parser.getSteps());
        project.setConcurrent(parser.isConcurrent());
        project.setWorkflowName(parser.getName());
        project.setWorkflowDescription(parser.getDescription());
        project.setLastModifiedTime();
        project.setWorkflowVersion(workflow.getVersion());
        if (username != null) {
            project.setLastModifiedBy(username);
        }
        if (isSyncProject) {
            this.createOrUpdateGitFile(null, encryptedToken, project, oldName, userId);
        }

        this.pubTriggerEvent(parser, project, userId, encryptedToken);
        this.projectRepository.updateByWorkflowRef(project);
        this.workflowRepository.add(workflow);
        // 返回是否并发执行流程实例
        return !concurrent && project.isConcurrent();
    }

    // 校验项目增删改查权限
    private void checkProjectPermission(String associationId, String associationType, Project project) {
        if (associationId != null && associationType != null &&
                (!associationId.equals(project.getAssociationId()) || !associationType.equals(project.getAssociationType()))) {
            throw new NoAssociatedPermissionException("无此仓库权限", project.getAssociationId(), project.getAssociationType());
        }
    }

    private void deleteGitFile(String encryptedToken, Project project, String userId) {
        if (!AssociationUtil.AssociationType.GIT_REPO.name().equals(project.getAssociationType())) {
            return;
        }
        var gitRepo = this.gitRepoRepository.findById(project.getAssociationId())
                .orElseThrow(() -> new DataNotFoundException("未找到Git仓库"));
        var branch = gitRepo.findFlowByProjectId(project.getId())
                .map(Flow::getBranchName)
                .orElseThrow(() -> new DataNotFoundException("未找到Git仓库项目"));
        var filepath = dslLocation + project.getWorkflowName() + ".yml";
        try {
            var oAuth2Api = OAuth2ApiProxy.builder()
                    .thirdPartyType(ThirdPartyTypeEnum.valueOf(this.oAuth2Properties.getThirdPartyType()))
                    .userId(userId)
                    .build();
            var accessToken = AESEncryptionUtil.decrypt(encryptedToken, this.oAuth2Properties.getClientSecret());
            try {
                oAuth2Api.getFile(accessToken, gitRepo.getOwner(), gitRepo.getRef(), filepath, branch);
                oAuth2Api.deleteFile(accessToken, gitRepo.getOwner(), gitRepo.getRef(), "", filepath, null, null, null, null, branch, "refactor: delete " + filepath);
            } catch (UnknownException e) {
                logger.info("Git 项目dsl文件不存在: " + filepath);
            }
        } catch (Exception e) {
            throw new RuntimeException("删除项目失败: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteById(String id, String associationId, String associationType, String encryptedToken, String userId) {
        Project project = this.projectRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("未找到该项目"));
        this.checkProjectPermission(associationId, associationType, project);
        var running = this.workflowInstanceRepository
                .findByRefAndStatuses(project.getWorkflowRef(), List.of(ProcessStatus.INIT, ProcessStatus.RUNNING, ProcessStatus.SUSPENDED))
                .size();
        if (running > 0) {
            throw new RuntimeException("仍有流程执行中，不能删除");
        }
        var projectLinkGroup = this.projectLinkGroupRepository.findByProjectId(id)
                .orElseThrow(() -> new DataNotFoundException("未找到项目分组"));
        this.projectLinkGroupRepository.deleteById(projectLinkGroup.getId());
        this.projectGroupRepository.subProjectCountById(projectLinkGroup.getProjectGroupId(), 1);
        this.deleteGitFile(encryptedToken, project, userId);
        this.projectRepository.deleteByWorkflowRef(project.getWorkflowRef());
        this.projectLastExecutionRepository.deleteByRef(project.getWorkflowRef());
        this.workflowRepository.deleteByRef(project.getWorkflowRef());
        this.workflowInstanceRepository.deleteByWorkflowRef(project.getWorkflowRef());
        this.asyncTaskInstanceRepository.deleteByWorkflowRef(project.getWorkflowRef());
        this.taskInstanceRepository.deleteByWorkflowRef(project.getWorkflowRef());
        this.publisher.publishEvent(new DeletedEvent(project.getId(), project.getAssociationId(), project.getAssociationType()));
    }

    @Transactional
    public void autoClean() {
        // 是否需要自动清理
        if (!this.globalProperties.getGlobal().getRecord().getAutoClean()) {
            return;
        }
        logger.info("执行记录自动清理已开启，将自动删除最新{}条之前的记录", this.globalProperties.getGlobal().getRecord().getMax());
        this.projectRepository.findAll().forEach(project -> {
            this.workflowInstanceRepository.findByRefOffset(project.getWorkflowRef(), this.globalProperties.getGlobal().getRecord().getMax())
                    .stream()
                    .filter(workflowInstance -> workflowInstance.getStatus() == ProcessStatus.FINISHED || workflowInstance.getStatus() == ProcessStatus.TERMINATED)
                    .forEach(workflowInstance -> {
                        this.workflowInstanceRepository.deleteById(workflowInstance.getId());
                        this.asyncTaskInstanceRepository.deleteByWorkflowInstanceId(workflowInstance.getId());
                        this.taskInstanceRepository.deleteByTriggerId(workflowInstance.getTriggerId());
                    });
        });

    }

    private void pubTriggerEvent(DslParser parser, Project project, String userId, String encryptedToken) {
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
                    .userId(userId)
                    .encryptedToken(encryptedToken)
                    .build();
            this.publisher.publishEvent(webhookEvent);
        }
        // 当触发类型为手动时
        if (project.getTriggerType() == Project.TriggerType.MANUAL) {
            var manualEvent = ManualEvent.builder()
                    .projectId(project.getId())
                    .associationId(project.getAssociationId())
                    .associationType(project.getAssociationType())
                    .userId(userId)
                    .encryptedToken(encryptedToken)
                    .build();
            this.publisher.publishEvent(manualEvent);
        }
    }

    public List<Project> findAll() {
        return this.projectRepository.findAll();
    }

    public Optional<Project> findById(String dslId, String associationId, String associationType) {
        var projectOptional = this.projectRepository.findById(dslId);
        if (projectOptional.isEmpty()) {
            return projectOptional;
        }
        this.checkProjectPermission(associationId, associationType, projectOptional.get());
        return projectOptional;
    }

    public Workflow findByRefAndVersion(String ref, String version) {
        return this.workflowRepository.findByRefAndVersion(ref, version)
                .orElseThrow(() -> new DataNotFoundException("未找到该Workflow"));
    }

    public PageInfo<ProjectVo> findPageByGroupId(Integer pageNum, Integer pageSize, String projectGroupId, String workflowName, String sortType, String associationId, String associationType) {
        return this.projectRepository.findPageByGroupId(pageNum, pageSize, projectGroupId, workflowName, sortType, associationId, associationType);
    }

    public Optional<Project> findByWorkflowRef(String workflowRef) {
        return this.projectRepository.findByWorkflowRef(workflowRef);
    }

    public Optional<Project> findByName(String associationId, String associationType, String name) {
        return this.projectRepository.findByName(associationId, associationType, name);
    }
}
