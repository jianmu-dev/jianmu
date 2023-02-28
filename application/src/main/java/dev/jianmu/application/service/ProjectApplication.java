package dev.jianmu.application.service;

import com.github.pagehelper.PageInfo;
import dev.jianmu.application.dsl.DslParser;
import dev.jianmu.application.event.CronEvent;
import dev.jianmu.application.event.ManualEvent;
import dev.jianmu.application.event.WebhookEvent;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.query.CustomWebhookDefApi;
import dev.jianmu.application.query.NodeDefApi;
import dev.jianmu.application.util.AssociationUtil;
import dev.jianmu.application.util.DslUtil;
import dev.jianmu.git.repo.aggregate.Flow;
import dev.jianmu.git.repo.repository.AccessTokenRepository;
import dev.jianmu.git.repo.repository.GitRepoRepository;
import dev.jianmu.infrastructure.GlobalProperties;
import dev.jianmu.infrastructure.mybatis.project.ProjectRepositoryImpl;
import dev.jianmu.jianmu_user_context.holder.UserSessionHolder;
import dev.jianmu.oauth2.api.config.OAuth2Properties;
import dev.jianmu.oauth2.api.enumeration.ThirdPartyTypeEnum;
import dev.jianmu.oauth2.api.exception.UnknownException;
import dev.jianmu.oauth2.api.impl.OAuth2ApiProxy;
import dev.jianmu.project.aggregate.Project;
import dev.jianmu.project.aggregate.ProjectGroup;
import dev.jianmu.project.aggregate.ProjectLastExecution;
import dev.jianmu.project.aggregate.ProjectLinkGroup;
import dev.jianmu.infrastructure.storage.StorageService;
import dev.jianmu.project.event.CreatedEvent;
import dev.jianmu.project.event.DeletedEvent;
import dev.jianmu.project.event.MovedEvent;
import dev.jianmu.project.event.TriggerEvent;
import dev.jianmu.project.query.ProjectVo;
import dev.jianmu.project.repository.ProjectGroupRepository;
import dev.jianmu.project.repository.ProjectLastExecutionRepository;
import dev.jianmu.project.repository.ProjectLinkGroupRepository;
import dev.jianmu.task.repository.InstanceParameterRepository;
import dev.jianmu.task.repository.TaskInstanceRepository;
import dev.jianmu.trigger.aggregate.Trigger;
import dev.jianmu.trigger.aggregate.Webhook;
import dev.jianmu.trigger.repository.CustomWebhookDefinitionVersionRepository;
import dev.jianmu.trigger.repository.TriggerEventRepository;
import dev.jianmu.trigger.service.WebhookOnlyService;
import dev.jianmu.trigger.repository.WebRequestRepository;
import dev.jianmu.workflow.aggregate.definition.Workflow;
import dev.jianmu.workflow.aggregate.process.ProcessStatus;
import dev.jianmu.workflow.repository.AsyncTaskInstanceRepository;
import dev.jianmu.workflow.repository.WorkflowInstanceRepository;
import dev.jianmu.workflow.repository.WorkflowRepository;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    public static final String committer = "jianmu";
    public static final String committerEmail = "dev@jianmu.dev";
    private static final Logger logger = LoggerFactory.getLogger(ProjectApplication.class);
    private static final String dslLocation = ".devops/";
    private final ProjectRepositoryImpl projectRepository;
    private final WorkflowRepository workflowRepository;
    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final AsyncTaskInstanceRepository asyncTaskInstanceRepository;
    private final TaskInstanceRepository taskInstanceRepository;
    private final CustomWebhookDefApi customWebhookDefApi;
    private final NodeDefApi nodeDefApi;
    private final ApplicationEventPublisher publisher;
    private final ProjectLinkGroupRepository projectLinkGroupRepository;
    private final ProjectGroupRepository projectGroupRepository;
    private final GlobalProperties globalProperties;
    private final TriggerEventRepository triggerEventRepository;
    private final ProjectLastExecutionRepository projectLastExecutionRepository;
    private final GitRepoRepository gitRepoRepository;
    private final OAuth2Properties oAuth2Properties;
    private final CustomWebhookDefinitionVersionRepository webhookDefinitionVersionRepository;
    private final WebhookOnlyService webhookOnlyService;
    private final AccessTokenRepository accessTokenRepository;
    private final AssociationUtil associationUtil;
    private final InstanceParameterRepository instanceParameterRepository;
    private final StorageService storageService;
    private final WebRequestRepository webRequestRepository;

    public ProjectApplication(
            ProjectRepositoryImpl projectRepository,
            WorkflowRepository workflowRepository,
            WorkflowInstanceRepository workflowInstanceRepository,
            AsyncTaskInstanceRepository asyncTaskInstanceRepository,
            TaskInstanceRepository taskInstanceRepository,
            CustomWebhookDefApi customWebhookDefApi,
            NodeDefApi nodeDefApi,
            ApplicationEventPublisher publisher,
            ProjectLinkGroupRepository projectLinkGroupRepository,
            ProjectGroupRepository projectGroupRepository,
            GlobalProperties globalProperties,
            TriggerEventRepository triggerEventRepository,
            ProjectLastExecutionRepository projectLastExecutionRepository,
            GitRepoRepository gitRepoRepository,
            OAuth2Properties oAuth2Properties,
            CustomWebhookDefinitionVersionRepository webhookDefinitionVersionRepository,
            WebhookOnlyService webhookOnlyService,
            AccessTokenRepository accessTokenRepository,
            AssociationUtil associationUtil,
            InstanceParameterRepository instanceParameterRepository,
            StorageService storageService,
            WebRequestRepository webRequestRepository
    ) {
        this.projectRepository = projectRepository;
        this.workflowRepository = workflowRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.asyncTaskInstanceRepository = asyncTaskInstanceRepository;
        this.taskInstanceRepository = taskInstanceRepository;
        this.customWebhookDefApi = customWebhookDefApi;
        this.nodeDefApi = nodeDefApi;
        this.publisher = publisher;
        this.projectLinkGroupRepository = projectLinkGroupRepository;
        this.projectGroupRepository = projectGroupRepository;
        this.globalProperties = globalProperties;
        this.triggerEventRepository = triggerEventRepository;
        this.projectLastExecutionRepository = projectLastExecutionRepository;
        this.gitRepoRepository = gitRepoRepository;
        this.oAuth2Properties = oAuth2Properties;
        this.webhookDefinitionVersionRepository = webhookDefinitionVersionRepository;
        this.webhookOnlyService = webhookOnlyService;
        this.accessTokenRepository = accessTokenRepository;
        this.associationUtil = associationUtil;
        this.instanceParameterRepository = instanceParameterRepository;
        this.storageService = storageService;
        this.webRequestRepository = webRequestRepository;
    }

    public void switchEnabled(String accountId, String projectId, boolean enabled) {
        var project = this.projectRepository.findById(projectId)
                .orElseThrow(() -> new DataNotFoundException("未找到该项目，项目id: " + projectId));
        project.switchEnabled(enabled);
        project.setLastModifiedById(accountId);
        this.projectRepository.updateByWorkflowRef(project);
    }

    public void trigger(String projectId, String triggerId, String triggerType, LocalDateTime occurredTime) {
        MDC.put("triggerId", triggerId);
        var project = this.projectRepository.findById(projectId)
                .orElseThrow(() -> new DataNotFoundException("未找到该项目，项目id: " + projectId));
        var triggerEvent = TriggerEvent.Builder.aTriggerEvent()
                .projectId(project.getId())
                .triggerId(triggerId)
                .triggerType(triggerType)
                .workflowRef(project.getWorkflowRef())
                .workflowVersion(project.getWorkflowVersion())
                .occurredTime(occurredTime)
                .build();
        this.publisher.publishEvent(triggerEvent);
    }

    public TriggerEvent triggerByManual(String projectId, String associationId, String associationType, String associationPlatform) {
        var project = this.projectRepository.findById(projectId)
                .orElseThrow(() -> new DataNotFoundException("未找到该项目，项目id: " + projectId));
        this.associationUtil.checkProjectPermission(associationId, associationType, associationPlatform, project);

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
                .occurredTime(evt.getOccurredTime())
                .build();
        this.publisher.publishEvent(triggerEvent);
        return triggerEvent;
    }

    private Workflow createWorkflow(DslParser parser, String dslText, String ref) {
        // 保存Shell node定义
        var shellNodes = parser.getShellNodes();
        this.nodeDefApi.addShellNodes(shellNodes);

        // 查询相关的触发器定义
        var triggerTypes = parser.getTriggerTypes();
        var triggerDefs = this.customWebhookDefApi.getByTypes(triggerTypes);

        // 查询相关的节点定义
        var types = parser.getAsyncTaskTypes();
        var nodeDefs = this.nodeDefApi.getByTypes(types);

        // 根据节点定义与DSL节点列表创建Workflow
        var nodes = parser.createNodes(nodeDefs, triggerDefs);
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
    public Project createProject(String creatorId, String modifiedId, String dslText, String projectGroupId, String userId, String associationId, String associationType, String associationPlatform, String branch, boolean isSyncProject) {
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
                .concurrent(parser.getConcurrent())
                .lastModifiedBy("")
                .gitRepoId("")
                .dslSource(Project.DslSource.LOCAL)
                .triggerType(parser.getTriggerType())
                .dslType(parser.getType().equals(Workflow.Type.WORKFLOW) ? Project.DslType.WORKFLOW : Project.DslType.PIPELINE)
                .creatorId(creatorId)
                .lastModifiedById(modifiedId)
                .associationId(associationId)
                .associationType(associationType)
                .associationPlatform(associationPlatform)
                .build();
        // 添加分组
        if (projectGroupId == null) {
            projectGroupId = this.projectGroupRepository.findByName(DEFAULT_PROJECT_GROUP_NAME).map(ProjectGroup::getId)
                    .orElseThrow(() -> new DataNotFoundException("未找到默认项目组"));
        } else {
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

        this.pubTriggerEvent(parser, project, userId);
        this.projectRepository.add(project);
        this.projectLastExecutionRepository.add(new ProjectLastExecution(project.getWorkflowRef()));
        this.projectLinkGroupRepository.add(projectLinkGroup);
        this.projectGroupRepository.addProjectCountById(projectGroupId, 1);
        this.workflowRepository.add(workflow);
        this.publisher.publishEvent(CreatedEvent.Builder.aCreatedEvent()
                .projectId(project.getId())
                .branch(branch)
                .associationId(associationId)
                .associationType(associationType)
                .build());
        if (isSyncProject) {
            this.createOrUpdateGitFile(userId, branch, project, project.getWorkflowName());
        }
        return project;
    }

    private void createOrUpdateGitFile(String userId, String branch, Project project, String oldName) {
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
            var accessToken = this.accessTokenRepository.get();
            try {
                if (!project.getWorkflowName().equals(oldName)) {
                    var oldFilepath = dslLocation + oldName + ".yml";
                    oAuth2Api.deleteFile(accessToken, gitRepo.getOwner(), gitRepo.getRef(), "", oldFilepath, null, null, committerEmail, committer, branch, "refactor: delete " + oldFilepath);
                }
                oAuth2Api.getFile(accessToken, gitRepo.getOwner(), gitRepo.getRef(), filepath, branch);
                oAuth2Api.updateFile(accessToken, gitRepo.getOwner(), gitRepo.getRef(), dsl, filepath, null, null, committerEmail, committer, branch, "refactor: " + filepath);
            } catch (UnknownException e) {
                oAuth2Api.createFile(accessToken, gitRepo.getOwner(), gitRepo.getRef(), dsl, filepath, null, null, committerEmail, committer, branch, "feat: " + filepath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("创建或修改项目失败: " + e.getMessage());
        }
    }

    @Transactional
    public boolean updateProject(String accountId, String dslId, String dslText, String projectGroupId, String userId, String associationId, String associationType, String associationPlatform, boolean isSyncProject) {
        Project project = this.projectRepository.findById(dslId)
                .orElseThrow(() -> new DataNotFoundException("未找到该项目，项目id: " + dslId));
        this.associationUtil.checkProjectPermission(associationId, associationType, associationPlatform, project);
        var concurrent = project.getConcurrent();
        var oldName = project.getWorkflowName();
        // 移动项目到项目组
        this.publisher.publishEvent(new MovedEvent(project.getId(), projectGroupId));
        // diff DSL
        if (project.getDslText().equals(dslText)) {
            return false;
        }
        dslText = this.diffDsl(project.getDslText(), dslText);
        // 解析DSL,语法检查
        var parser = DslParser.parse(dslText);
        var workflow = this.createWorkflow(parser, dslText, project.getWorkflowRef());

        project.setDslText(dslText);
        project.setDslType(parser.getType().equals(Workflow.Type.WORKFLOW) ? Project.DslType.WORKFLOW : Project.DslType.PIPELINE);
        project.setTriggerType(parser.getTriggerType());
        project.setSteps(parser.getSteps());
        project.setConcurrent(parser.getConcurrent());
        project.setWorkflowName(parser.getName());
        project.setWorkflowDescription(parser.getDescription());
        project.setLastModifiedTime();
        project.setWorkflowVersion(workflow.getVersion());
        project.setLastModifiedById(accountId);
        project.setLastModifiedBy("");

        this.pubTriggerEvent(parser, project, userId);
        this.projectRepository.updateByWorkflowRef(project);
        this.workflowRepository.add(workflow);
        if (isSyncProject) {
            this.createOrUpdateGitFile(userId, null, project, oldName);
        }
        // 返回是否并发执行流程实例
        return project.getConcurrent() > concurrent;
    }

    @Transactional
    public void innerUpdateProject(String projectId, String dslText) {
        Project project = this.projectRepository.findById(projectId)
                .orElseThrow(() -> new DataNotFoundException("未找到该项目，项目id: " + projectId));
        // diff DSL
        if (project.getDslText().equals(dslText)) {
            return;
        }
        // 解析DSL,语法检查
        var parser = DslParser.parse(dslText);
        var workflow = this.createWorkflow(parser, dslText, project.getWorkflowRef());

        project.setDslText(dslText);
        project.setDslType(parser.getType().equals(Workflow.Type.WORKFLOW) ? Project.DslType.WORKFLOW : Project.DslType.PIPELINE);
        project.setTriggerType(parser.getTriggerType());
        project.setSteps(parser.getSteps());
        project.setConcurrent(parser.getConcurrent());
        project.setWorkflowName(parser.getName());
        project.setWorkflowDescription(parser.getDescription());
        project.setLastModifiedTime();
        project.setWorkflowVersion(workflow.getVersion());

        this.projectRepository.updateByWorkflowRef(project);
        this.workflowRepository.add(workflow);
    }

    private void deleteGitFile(Project project, String userId) {
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
            var accessToken = this.accessTokenRepository.get();
            try {
                oAuth2Api.getFile(accessToken, gitRepo.getOwner(), gitRepo.getRef(), filepath, branch);
                oAuth2Api.deleteFile(accessToken, gitRepo.getOwner(), gitRepo.getRef(), "", filepath, null, null, committerEmail, committer, branch, "refactor: delete " + filepath);
            } catch (UnknownException e) {
                logger.info("Git 项目dsl文件不存在: " + filepath);
            }
        } catch (Exception e) {
            throw new RuntimeException("删除项目失败: " + e.getMessage());
        }
    }

    // diff DSL
    private String diffDsl(String oldDsl, String newDsl) {
        var diff = DslUtil.diff(oldDsl, newDsl);
        // 忽略新增的raw-data
        if (!diff.isO1HasRawData() && diff.isO2HasRawData()) {
            return newDsl.replaceAll("\nraw-data: \"\\{.*}\"$", "");
        }
        // 对象不变时，保留raw-data
        if (diff.isDiff() && diff.isO1HasRawData() && !diff.isO2HasRawData()) {
            return newDsl + oldDsl.substring(oldDsl.indexOf("\nraw-data: \""));
        }
        return newDsl;
    }

    @Transactional
    public void deleteById(String id, String userId, String associationId, String associationType, String associationPlatform) {
        Project project = this.projectRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("未找到该项目, 项目id: " + id));
        this.associationUtil.checkProjectPermission(associationId, associationType, associationPlatform, project);
        var running = this.workflowInstanceRepository
                .findByRefAndStatuses(project.getWorkflowRef(), List.of(ProcessStatus.INIT, ProcessStatus.RUNNING, ProcessStatus.SUSPENDED))
                .size();
        if (running > 0) {
            throw new RuntimeException("仍有流程执行中，不能删除");
        }
        var projectLinkGroup = this.projectLinkGroupRepository.findByProjectId(id)
                .orElseThrow(() -> new DataNotFoundException("未找到项目分组， 项目id: " + id));
        this.projectLinkGroupRepository.deleteById(projectLinkGroup.getId());
        this.projectGroupRepository.subProjectCountById(projectLinkGroup.getProjectGroupId(), 1);
        this.deleteGitFile(project, userId);
        this.projectRepository.deleteByWorkflowRef(project.getWorkflowRef());
        this.projectLastExecutionRepository.deleteByRef(project.getWorkflowRef());
        this.workflowRepository.deleteByRef(project.getWorkflowRef());
        this.workflowInstanceRepository.deleteByWorkflowRef(project.getWorkflowRef());
        this.asyncTaskInstanceRepository.deleteByWorkflowRef(project.getWorkflowRef());
        this.taskInstanceRepository.deleteByWorkflowRef(project.getWorkflowRef());
        this.publisher.publishEvent(DeletedEvent.Builder.aDeletedEvent()
                .projectId(project.getId())
                .userId(userId)
                .associationId(project.getAssociationId())
                .associationType(project.getAssociationType())
                .build());
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
                        // 删除流程实例
                        this.workflowInstanceRepository.deleteById(workflowInstance.getId());
                        this.storageService.deleteWorkflowLog(workflowInstance.getTriggerId());
                        // 删除触发器事件和web请求
                        this.webRequestRepository.findByTriggerId(workflowInstance.getTriggerId()).ifPresent(webRequest -> {
                            this.storageService.deleteWebhook(webRequest.getId());
                        });
                        this.triggerEventRepository.deleteEventAdnWebRequestByTriggerId(workflowInstance.getTriggerId());
                        this.triggerEventRepository.deleteParameterByTriggerId(workflowInstance.getTriggerId());
                        // 删除任务实例
                        this.taskInstanceRepository.findByTriggerId(workflowInstance.getTriggerId()).stream()
                                .filter(taskInstance -> !taskInstance.getAsyncTaskRef().equalsIgnoreCase("start"))
                                .filter(taskInstance -> !taskInstance.getAsyncTaskRef().equalsIgnoreCase("end"))
                                .forEach(taskInstance -> this.storageService.deleteTaskLog(taskInstance.getId()));
                        this.asyncTaskInstanceRepository.deleteByWorkflowInstanceId(workflowInstance.getId());
                        this.taskInstanceRepository.deleteByTriggerId(workflowInstance.getTriggerId());
                        this.instanceParameterRepository.deleteByTriggerId(workflowInstance.getTriggerId());
                    });
        });

    }

    // 清除流程实例旧数据
    @Transactional
    public void cleanOldData() {
        logger.info("开始清除执行记录旧数据");
        this.projectRepository.findAll().forEach(project -> {
            this.workflowInstanceRepository.findOldDataByRefOffset(project.getWorkflowRef(), this.globalProperties.getGlobal().getRecord().getMax())
                    .stream()
                    .filter(workflowInstance -> workflowInstance.getStatus() == ProcessStatus.FINISHED || workflowInstance.getStatus() == ProcessStatus.TERMINATED)
                    .forEach(workflowInstance -> {
                        this.storageService.deleteWorkflowLog(workflowInstance.getTriggerId());
                        // 删除触发器事件和web请求
                        this.webRequestRepository.findByTriggerId(workflowInstance.getTriggerId()).ifPresent(webRequest -> {
                            this.storageService.deleteWebhook(webRequest.getId());
                        });
                        this.triggerEventRepository.deleteEventAdnWebRequestByTriggerId(workflowInstance.getTriggerId());
                        this.triggerEventRepository.deleteParameterByTriggerId(workflowInstance.getTriggerId());
                        // 删除任务实例
                        this.taskInstanceRepository.findByTriggerId(workflowInstance.getTriggerId()).stream()
                                .filter(taskInstance -> !taskInstance.getAsyncTaskRef().equalsIgnoreCase("start"))
                                .filter(taskInstance -> !taskInstance.getAsyncTaskRef().equalsIgnoreCase("end"))
                                .forEach(taskInstance -> this.storageService.deleteTaskLog(taskInstance.getId()));
                        this.instanceParameterRepository.deleteByTriggerId(workflowInstance.getTriggerId());
                    });
        });
        logger.info("结束清除执行记录旧数据");
    }

    private void pubTriggerEvent(DslParser parser, Project project, String userId) {
        // 创建Cron触发器
        if (project.getTriggerType() == Project.TriggerType.CRON) {
            if (!CronExpression.isValidExpression(parser.getCron())) {
                throw new RuntimeException("cron表达式不合法");
            }
            var cronEvent = CronEvent.builder()
                    .projectId(project.getId())
                    .schedule(parser.getCron())
                    .build();
            this.publisher.publishEvent(cronEvent);
        }
        // 创建Webhook触发器
        if (project.getTriggerType() == Project.TriggerType.WEBHOOK) {
            if (parser.getCustomWebhooks().isEmpty()) {
                var webhookEvent = WebhookEvent.builder()
                        .projectId(project.getId())
                        .userId(userId)
                        .webhook(Webhook.Builder.aWebhook()
                                .only(parser.getWebhook().getOnly())
                                .auth(parser.getWebhook().getAuth())
                                .param(parser.getWebhook().getParam())
                                .build())
                        .build();
                this.publisher.publishEvent(webhookEvent);
            }
            parser.getCustomWebhooks().forEach(dslWebhook -> {
                var webhookDefinitionVersion = this.webhookDefinitionVersionRepository.findByType(dslWebhook.getWebhookType())
                        .orElseThrow(() -> new DataNotFoundException("未找到Webhook: " + dslWebhook.getWebhookType()));
                var events = this.webhookOnlyService.findEvents(webhookDefinitionVersion.getEvents(), dslWebhook.getEvent());
                var webhookEvent = WebhookEvent.builder()
                        .projectId(project.getId())
                        .userId(userId)
                        .webhook(Webhook.Builder.aWebhook()
                                .events(events)
                                .build())
                        .webhookType(dslWebhook.getWebhookType())
                        .eventInstances(dslWebhook.getEvent())
                        .build();
                this.publisher.publishEvent(webhookEvent);
            });
        }
        // 当触发类型为手动时
        if (project.getTriggerType() == Project.TriggerType.MANUAL) {
            var manualEvent = ManualEvent.builder()
                    .projectId(project.getId())
                    .userId(userId)
                    .associationId(project.getAssociationId())
                    .associationType(project.getAssociationType())
                    .build();
            this.publisher.publishEvent(manualEvent);
        }
    }

    public List<Project> findAll() {
        return this.projectRepository.findAll();
    }

    public Optional<Project> findById(String dslId, String associationId, String associationType, String associationPlatform) {
        var projectOptional = this.projectRepository.findById(dslId);
        if (projectOptional.isEmpty()) {
            return projectOptional;
        }
        this.associationUtil.checkProjectViewPermission(associationId, associationType, associationPlatform, projectOptional.get());
        return projectOptional;
    }

    public Workflow findByRefAndVersion(String ref, String version) {
        return this.workflowRepository.findByRefAndVersion(ref, version)
                .orElseThrow(() -> new DataNotFoundException("未找到该Workflow"));
    }

    public PageInfo<ProjectVo> findPageByGroupId(Integer pageNum, Integer pageSize, String projectGroupId, String workflowName, String sortType, String associationId, String associationType, String associationPlatform) {
        return this.projectRepository.findPageByGroupId(pageNum, pageSize, projectGroupId, workflowName, sortType, associationId, associationType, associationPlatform);
    }

    public Optional<Project> findByWorkflowRef(String workflowRef) {
        return this.projectRepository.findByWorkflowRef(workflowRef);
    }

    public Optional<Project> findByName(String associationId, String associationType, String associationPlatform, String name) {
        return this.projectRepository.findByName(associationId, associationType, associationPlatform, name);
    }

    public List<ProjectVo> findByIds(List<String> ids) {
        return this.projectRepository.findVoByIdIn(ids);
    }
}
