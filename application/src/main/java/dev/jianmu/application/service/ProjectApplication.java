package dev.jianmu.application.service;

import com.github.pagehelper.PageInfo;
import dev.jianmu.application.dsl.DslParser;
import dev.jianmu.application.event.CronEvent;
import dev.jianmu.application.event.ManualEvent;
import dev.jianmu.application.event.WebhookEvent;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.query.NodeDefApi;
import dev.jianmu.application.util.DslUtil;
import dev.jianmu.el.ElContext;
import dev.jianmu.infrastructure.GlobalProperties;
import dev.jianmu.infrastructure.jgit.JgitService;
import dev.jianmu.infrastructure.mybatis.project.ProjectRepositoryImpl;
import dev.jianmu.infrastructure.storage.StorageService;
import dev.jianmu.project.aggregate.*;
import dev.jianmu.project.event.*;
import dev.jianmu.project.query.ProjectVo;
import dev.jianmu.project.repository.*;
import dev.jianmu.secret.aggregate.CredentialManager;
import dev.jianmu.task.aggregate.TaskInstance;
import dev.jianmu.task.aggregate.Volume;
import dev.jianmu.task.event.VolumeCreatedEvent;
import dev.jianmu.task.event.VolumeDeletedEvent;
import dev.jianmu.task.repository.InstanceParameterRepository;
import dev.jianmu.task.repository.TaskInstanceRepository;
import dev.jianmu.trigger.aggregate.Trigger;
import dev.jianmu.trigger.aggregate.WebRequest;
import dev.jianmu.trigger.aggregate.Webhook;
import dev.jianmu.trigger.event.TriggerEventParameter;
import dev.jianmu.trigger.repository.TriggerEventRepository;
import dev.jianmu.trigger.repository.TriggerRepository;
import dev.jianmu.trigger.repository.WebRequestRepository;
import dev.jianmu.workflow.aggregate.definition.Workflow;
import dev.jianmu.workflow.aggregate.parameter.Parameter;
import dev.jianmu.workflow.aggregate.process.ProcessStatus;
import dev.jianmu.workflow.aggregate.process.WorkflowInstance;
import dev.jianmu.workflow.el.ExpressionLanguage;
import dev.jianmu.workflow.repository.AsyncTaskInstanceRepository;
import dev.jianmu.workflow.repository.ParameterRepository;
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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    private final AsyncTaskInstanceRepository asyncTaskInstanceRepository;
    private final TaskInstanceRepository taskInstanceRepository;
    private final NodeDefApi nodeDefApi;
    private final ApplicationEventPublisher publisher;
    private final JgitService jgitService;
    private final ProjectLinkGroupRepository projectLinkGroupRepository;
    private final ProjectGroupRepository projectGroupRepository;
    private final GlobalProperties globalProperties;
    private final TriggerEventRepository triggerEventRepository;
    private final ProjectLastExecutionRepository projectLastExecutionRepository;
    private final InstanceParameterRepository instanceParameterRepository;
    private final StorageService storageService;
    private final WebRequestRepository webRequestRepository;
    private final TriggerRepository triggerRepository;
    private final CredentialManager credentialManager;
    private final ExpressionLanguage expressionLanguage;
    private final ParameterRepository parameterRepository;
    private final TrashProjectRepository trashProjectRepository;

    public ProjectApplication(
        ProjectRepositoryImpl projectRepository,
        GitRepoRepository gitRepoRepository,
        WorkflowRepository workflowRepository,
        WorkflowInstanceRepository workflowInstanceRepository,
        AsyncTaskInstanceRepository asyncTaskInstanceRepository,
        TaskInstanceRepository taskInstanceRepository,
        NodeDefApi nodeDefApi,
        ApplicationEventPublisher publisher,
        JgitService jgitService,
        ProjectLinkGroupRepository projectLinkGroupRepository,
        ProjectGroupRepository projectGroupRepository,
        GlobalProperties globalProperties,
        TriggerEventRepository triggerEventRepository,
        ProjectLastExecutionRepository projectLastExecutionRepository,
        InstanceParameterRepository instanceParameterRepository,
        StorageService storageService,
        WebRequestRepository webRequestRepository,
        TriggerRepository triggerRepository,
        CredentialManager credentialManager,
        ExpressionLanguage expressionLanguage,
        ParameterRepository parameterRepository,
        TrashProjectRepository trashProjectRepository
    ) {
        this.projectRepository = projectRepository;
        this.gitRepoRepository = gitRepoRepository;
        this.workflowRepository = workflowRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.asyncTaskInstanceRepository = asyncTaskInstanceRepository;
        this.taskInstanceRepository = taskInstanceRepository;
        this.nodeDefApi = nodeDefApi;
        this.publisher = publisher;
        this.jgitService = jgitService;
        this.projectLinkGroupRepository = projectLinkGroupRepository;
        this.projectGroupRepository = projectGroupRepository;
        this.globalProperties = globalProperties;
        this.triggerEventRepository = triggerEventRepository;
        this.projectLastExecutionRepository = projectLastExecutionRepository;
        this.instanceParameterRepository = instanceParameterRepository;
        this.storageService = storageService;
        this.webRequestRepository = webRequestRepository;
        this.triggerRepository = triggerRepository;
        this.credentialManager = credentialManager;
        this.expressionLanguage = expressionLanguage;
        this.parameterRepository = parameterRepository;
        this.trashProjectRepository = trashProjectRepository;
    }

    public void switchEnabled(String projectId, boolean enabled) {
        var project = this.projectRepository.findById(projectId)
                .orElseThrow(() -> new DataNotFoundException("未找到该项目，项目id: " + projectId));
        project.switchEnabled(enabled);
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

    @Transactional
    public TriggerEvent triggerByManual(String projectId, Map<String, Object> triggerParam) {
        var project = this.projectRepository.findById(projectId)
                .orElseThrow(() -> new DataNotFoundException("未找到该项目，项目id: " + projectId));
        if (!project.isEnabled()) {
            throw new RuntimeException("当前项目不可触发，请先修改状态");
        }
        var triggerOptional = this.triggerRepository.findByProjectId(projectId);
        if (triggerOptional.isPresent() && triggerOptional.get().getType() == Trigger.Type.WEBHOOK) {
            // 手动触发webhook项目
            return this.saveTriggerEventParams(project, triggerOptional.get().getWebhook(), triggerParam);
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
                .occurredTime(evt.getOccurredTime())
                .build();
        this.publisher.publishEvent(triggerEvent);
        return triggerEvent;
    }

    private TriggerEvent saveTriggerEventParams(Project project, Webhook webhook, Map<String, Object> triggerParam) {
        var eventParameters = new ArrayList<TriggerEventParameter>();
        var parameters = new ArrayList<Parameter>();
        if (webhook.getParam() != null) {
            webhook.getParam().forEach(webhookParameter -> {
                var value = triggerParam.get(webhookParameter.getName());
                if (webhookParameter.isRequired() && value == null) {
                    throw new RuntimeException("未填写参数：" + webhookParameter.getName());
                }
                var parameter = Parameter.Type
                        .getTypeByName(webhookParameter.getType())
                        .newParameter(value == null ? webhookParameter.getDefaultValue() : value);
                var eventParameter = TriggerEventParameter.Builder.aTriggerParameter()
                        .name(webhookParameter.getName())
                        .type(webhookParameter.getType())
                        .value(parameter.getStringValue())
                        .parameterId(parameter.getId())
                        .build();
                parameters.add(parameter);
                eventParameters.add(eventParameter);
            });
        }
        // 验证Auth
        if (webhook.getAuth() != null) {
            var authValue = this.findSecret(webhook.getAuth().getValue());
            var authToken = this.findEventParam(eventParameters, webhook.getAuth().getToken(), true);
            if (!authValue.equals(authToken)) {
                throw new RuntimeException("Auth验证失败，与密钥不匹配");
            }
        }
        // 验证only
        if (webhook.getOnly() != null) {
            var el = this.findEventParam(eventParameters, webhook.getOnly(), false);
            // 计算参数表达式
            var expression = expressionLanguage.parseExpression(el);
            var evaluationResult = expressionLanguage.evaluateExpression(expression, new ElContext());
            if (evaluationResult.isFailure()) {
                var errorMsg = "表达式：" + el + " 计算错误: " + evaluationResult.getFailureMessage();
                throw new RuntimeException(errorMsg);
            }
            var res = evaluationResult.getValue();
            if (res.getType() != Parameter.Type.BOOL || !((Boolean) res.getValue())) {
                throw new RuntimeException("Only计算不匹配，计算结果为：" + res.getStringValue());
            }
        }
        // 过滤SECRET类型参数不保存
        var eventParametersClean = eventParameters.stream()
                .filter(triggerEventParameter -> !triggerEventParameter.getType().equals("SECRET"))
                .collect(Collectors.toList());
        var parametersClean = parameters.stream()
                .filter(parameter -> !(parameter.getType() == Parameter.Type.SECRET))
                .collect(Collectors.toList());
        var event = dev.jianmu.trigger.event.TriggerEvent.Builder
                .aTriggerEvent()
                .projectId(project.getId())
                .triggerId("")
                .triggerType(Trigger.Type.MANUAL.name())
                .parameters(eventParametersClean)
                .build();
        this.triggerEventRepository.save(event);
        this.parameterRepository.addAll(parametersClean);

        MDC.put("triggerId", event.getId());
        var triggerEvent = TriggerEvent.Builder.aTriggerEvent()
                .projectId(project.getId())
                .triggerId(event.getId())
                .triggerType(Trigger.Type.MANUAL.name())
                .workflowRef(project.getWorkflowRef())
                .workflowVersion(project.getWorkflowVersion())
                .occurredTime(event.getOccurredTime())
                .build();
        this.publisher.publishEvent(triggerEvent);
        return triggerEvent;
    }

    private String findEventParam(List<TriggerEventParameter> params, String exp, boolean isAuth) {
        String pattern = "\\$\\{trigger\\.(.*?)}";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(exp);
        while (m.find()) {
            String name = m.group(1);
            var parameter = params.stream()
                    .filter(t -> t.getName().equals(name))
                    .findFirst()
                    .orElse(null);
            if (parameter != null) {
                var value = this.findParameterString(parameter, isAuth);
                exp = exp.replaceAll("\\$\\{trigger\\." + name + "}", value);
            }
        }
        return exp;
    }

    private String findParameterString(TriggerEventParameter parameter, boolean isAuth) {
        if (isAuth || parameter.getType().equals(Parameter.Type.BOOL.name()) || parameter.getType().equals(Parameter.Type.NUMBER.name())) {
            return parameter.getValue();
        }
        return "\"" + parameter.getValue() + "\"";
    }

    private String findSecret(String secretExp) {
        var secret = this.isSecret(secretExp);
        if (secret == null) {
            throw new IllegalArgumentException("密钥参数格式错误：" + secretExp);
        }
        // 处理密钥类型参数, 获取值后转换为String类型参数
        var strings = secret.split("\\.");
        var kv = this.credentialManager.findByNamespaceNameAndKey(strings[0], strings[1])
                .orElseThrow(() -> new RuntimeException("Auth验证失败，未找到密钥"));
        return kv.getValue();
    }

    private String isSecret(String paramValue) {
        Pattern pattern = Pattern.compile("^\\(\\(([a-zA-Z0-9_-]+\\.+[a-zA-Z0-9_-]+)\\)\\)$");
        Matcher matcher = pattern.matcher(paramValue);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
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
                .caches(parser.getCaches())
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
                .enabled(parser.isEnabled())
                .mutable(parser.isMutable())
                .concurrent(parser.getConcurrent())
                .gitRepoId(gitRepo.getId())
                .dslSource(Project.DslSource.GIT)
                .triggerType(parser.getTriggerType())
                .dslType(parser.getType().equals(Workflow.Type.WORKFLOW) ? Project.DslType.WORKFLOW : Project.DslType.PIPELINE)
                .lastModifiedBy("admin")
                .build();
        // 添加到默认分组
        String groupId;
        if (projectGroupId != null) {
            this.projectGroupRepository.findById(projectGroupId).orElseThrow(() -> new DataNotFoundException("未找到该项目组，项目组id: " + projectGroupId));
            groupId = projectGroupId;
        } else {
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
                .orElseThrow(() -> new DataNotFoundException("未找到该项目，项目id: " + projectId));
        var lastWorkflowVersion = project.getWorkflowVersion();
        var concurrent = project.getConcurrent();
        var gitRepo = this.gitRepoRepository.findById(project.getGitRepoId())
                .orElseThrow(() -> new DataNotFoundException("未找到Git仓库，git仓库id: " + project.getGitRepoId()));
        var dslText = this.jgitService.readDsl(gitRepo.getId(), gitRepo.getDslPath());
        // 解析DSL,语法检查
        var parser = DslParser.parse(dslText);
        var workflow = this.createWorkflow(parser, dslText, project.getWorkflowRef());
        project.setDslText(dslText);
        project.setDslType(parser.getType().equals(Workflow.Type.WORKFLOW) ? Project.DslType.WORKFLOW : Project.DslType.PIPELINE);
        project.setTriggerType(Project.TriggerType.MANUAL);
        project.setLastModifiedBy("admin");
        project.setSteps(parser.getSteps());
        project.setEnabled(parser.isEnabled());
        project.setMutable(parser.isMutable());
        project.setConcurrent(parser.getConcurrent());
        project.setWorkflowName(parser.getName());
        project.setWorkflowDescription(parser.getDescription());
        project.setLastModifiedTime();
        project.setWorkflowVersion(workflow.getVersion());
        project.setTriggerType(parser.getTriggerType());

        this.pubTriggerEvent(parser, project);
        this.publishCacheEvent(workflow, lastWorkflowVersion);
        this.projectRepository.updateByWorkflowRef(project);
        this.workflowRepository.add(workflow);
        this.jgitService.cleanUp(gitRepo.getId());
        if (project.getConcurrent() > concurrent) {
            this.concurrentWorkflowInstance(workflow.getRef());
        }
    }

    // 并发流程实例
    private void concurrentWorkflowInstance(String workflowRef) {
        var project = this.projectRepository.findByWorkflowRef(workflowRef)
                .orElseThrow(() -> new DataNotFoundException("未找到项目, ref: " + workflowRef));
        var projectLastExecution = this.projectLastExecutionRepository.findByRef(project.getWorkflowRef())
                .orElseThrow(() -> new DataNotFoundException("未找到项目最后执行记录, ref: " + workflowRef));
        int i = this.workflowInstanceRepository
                .findByRefAndStatuses(workflowRef, List.of(ProcessStatus.RUNNING, ProcessStatus.SUSPENDED))
                .size();
        if (project.getConcurrent() > i) {
            this.workflowInstanceRepository.findByRefAndStatuses(workflowRef, List.of(ProcessStatus.INIT))
                    .stream().limit(project.getConcurrent() - i)
                    .forEach(workflowInstance -> {
                        workflowInstance.start();
                        if (!this.workflowInstanceRepository.running(workflowInstance)) {
                            return;
                        }
                        // 修改项目最后执行状态
                        projectLastExecution.running(workflowInstance.getId(), workflowInstance.getSerialNo(), workflowInstance.getStartTime(), workflowInstance.getStatus().name());
                        this.projectLastExecutionRepository.update(projectLastExecution);
                    });
        }
    }

    @Transactional
    public Project createProject(String dslText, String projectGroupId) {
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
                .enabled(parser.isEnabled())
                .mutable(parser.isMutable())
                .concurrent(parser.getConcurrent())
                .lastModifiedBy("admin")
                .gitRepoId("")
                .dslSource(Project.DslSource.LOCAL)
                .triggerType(parser.getTriggerType())
                .dslType(parser.getType().equals(Workflow.Type.WORKFLOW) ? Project.DslType.WORKFLOW : Project.DslType.PIPELINE)
                .build();
        // 添加分组
        this.projectGroupRepository.findById(projectGroupId).orElseThrow(() -> new DataNotFoundException("未找到该项目组，项目组id: " + projectGroupId));
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
        this.projectLastExecutionRepository.add(new ProjectLastExecution(project.getWorkflowRef()));
        this.projectLinkGroupRepository.add(projectLinkGroup);
        this.projectGroupRepository.addProjectCountById(projectGroupId, 1);
        this.workflowRepository.add(workflow);
        this.publisher.publishEvent(new CreatedEvent(project.getId()));
        this.publishCacheCreatedEvent(parser.getCaches(), workflow.getRef());
        return project;
    }

    private void publishCacheCreatedEvent(List<String> caches, String workflowRef) {
        if (caches == null) {
            return;
        }
        caches.forEach(cache -> this.publisher.publishEvent(VolumeCreatedEvent.aVolumeCreatedEvent()
                .name(cache)
                .scope(Volume.Scope.PROJECT)
                .workflowRef(workflowRef)
                .build()
        ));
    }

    @Transactional
    public void updateProject(String dslId, String dslText, String projectGroupId) {
        Project project = this.projectRepository.findById(dslId)
                .orElseThrow(() -> new DataNotFoundException("未找到该项目，项目id: " + dslId));
        var concurrent = project.getConcurrent();
        if (project.getDslSource() == Project.DslSource.GIT) {
            throw new IllegalArgumentException("不能修改通过Git导入的项目");
        }
        // 移动项目到项目组
        this.publisher.publishEvent(new MovedEvent(project.getId(), projectGroupId));
        // diff DSL
        if (project.getDslText().equals(dslText)) {
            return;
        }
        dslText = this.diffDsl(project.getDslText(), dslText);
        // 解析DSL,语法检查
        var parser = DslParser.parse(dslText);
        var workflow = this.createWorkflow(parser, dslText, project.getWorkflowRef());
        var lastWorkflowVersion = project.getWorkflowVersion();

        project.setDslText(dslText);
        project.setDslType(parser.getType().equals(Workflow.Type.WORKFLOW) ? Project.DslType.WORKFLOW : Project.DslType.PIPELINE);
        project.setTriggerType(parser.getTriggerType());
        project.setLastModifiedBy("admin");
        project.setSteps(parser.getSteps());
        project.setEnabled(parser.isEnabled());
        project.setMutable(parser.isMutable());
        project.setConcurrent(parser.getConcurrent());
        project.setWorkflowName(parser.getName());
        project.setWorkflowDescription(parser.getDescription());
        project.setLastModifiedTime();
        project.setWorkflowVersion(workflow.getVersion());

        this.pubTriggerEvent(parser, project);
        this.publishCacheEvent(workflow, lastWorkflowVersion);
        this.projectRepository.updateByWorkflowRef(project);
        this.workflowRepository.add(workflow);
        if (project.getConcurrent() > concurrent) {
            this.concurrentWorkflowInstance(workflow.getRef());
        }
    }

    private void publishCacheEvent(Workflow workflow, String lastWorkflowVersion) {
        var lastWorkflow = this.workflowRepository.findByRefAndVersion(workflow.getRef(), lastWorkflowVersion)
                .orElseThrow(() -> new DataNotFoundException(String.format("未找到workflow：%s-%s", workflow.getRef(), lastWorkflowVersion)));
        // 删除cache
        if (lastWorkflow.getCaches() != null) {
            lastWorkflow.getCaches().stream()
                    .filter(cache -> workflow.getCaches() == null || !workflow.getCaches().contains(cache))
                    .forEach(cache -> {
                        this.publisher.publishEvent(VolumeDeletedEvent.aVolumeDeletedEvent()
                                .name(cache)
                                .workflowRef(workflow.getRef())
                                .deletedType(VolumeDeletedEvent.VolumeDeletedType.NAME)
                                .build()
                        );
                    });
        }
        // 创建cache
        if (workflow.getCaches() != null) {
            workflow.getCaches().stream()
                    .filter(cache -> lastWorkflow.getCaches() == null || !lastWorkflow.getCaches().contains(cache))
                    .forEach(cache -> {
                        this.publisher.publishEvent(VolumeCreatedEvent.aVolumeCreatedEvent()
                                .name(cache)
                                .scope(Volume.Scope.PROJECT)
                                .workflowRef(workflow.getRef())
                                .build()
                        );
                    });
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
    public void deleteById(String id) {
        Project project = this.projectRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("未找到该项目, 项目id: " + id));
        var running = this.workflowInstanceRepository
                .findByRefAndStatuses(project.getWorkflowRef(), List.of(ProcessStatus.INIT, ProcessStatus.RUNNING, ProcessStatus.SUSPENDED))
                .size();
        if (running > 0) {
            throw new RuntimeException("仍有流程执行中，不能删除");
        }

        this.projectRepository.deleteByWorkflowRef(project.getWorkflowRef());
        this.trashProjectRepository.add(project);

        this.publisher.publishEvent(new DeletedEvent(project.getId()));
        this.publisher.publishEvent(VolumeDeletedEvent.aVolumeDeletedEvent()
                .workflowRef(project.getWorkflowRef())
                .deletedType(VolumeDeletedEvent.VolumeDeletedType.REF)
                .build());
        this.publisher.publishEvent(new TrashEvent(project.getId()));
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
                        this.triggerEventRepository.deleteByTriggerId(workflowInstance.getTriggerId());
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

    private void pubTriggerEvent(DslParser parser, Project project) {
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

    public PageInfo<ProjectVo> findPageByGroupId(Integer pageNum, Integer pageSize, String projectGroupId, String workflowName, String sortType) {
        return this.projectRepository.findPageByGroupId(pageNum, pageSize, projectGroupId, workflowName, sortType);
    }

    public List<ProjectVo> findByIds(List<String> ids) {
        return this.projectRepository.findVoByIdIn(ids);
    }

    public Workflow findLastWorkflowByRef(String ref) {
        var project = this.projectRepository.findByWorkflowRef(ref)
                .orElseThrow(() -> new DataNotFoundException("未找到该Workflow"));
        return this.workflowRepository.findByRefAndVersion(ref, project.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到该Workflow"));
    }

    @Transactional
    public void trashProject(String projectId) {
        var project = this.trashProjectRepository.findById(projectId)
            .orElseThrow(() -> new DataNotFoundException("未找到待删除项目：" + projectId));
        var projectLinkGroup = this.projectLinkGroupRepository.findByProjectId(projectId)
            .orElseThrow(() -> new DataNotFoundException("未找到项目分组， 项目id: " + projectId));
        var triggerIds = this.workflowInstanceRepository.findByRef(project.getWorkflowRef()).stream()
            .map(WorkflowInstance::getTriggerId)
            .collect(Collectors.toList());
        triggerIds.forEach(this.storageService::deleteWorkflowLog);
        this.webRequestRepository.findByProjectId(project.getId()).stream()
            .map(WebRequest::getId)
            .forEach(this.storageService::deleteWebhook);
        this.taskInstanceRepository.findIdAndRefByWorkflowRef(project.getWorkflowRef()).stream()
            .filter(taskInstance -> !taskInstance.getAsyncTaskRef().equalsIgnoreCase("start"))
            .filter(taskInstance -> !taskInstance.getAsyncTaskRef().equalsIgnoreCase("end"))
            .map(TaskInstance::getId)
            .forEach(this.storageService::deleteTaskLog);

        this.projectLinkGroupRepository.deleteById(projectLinkGroup.getId());
        this.projectGroupRepository.subProjectCountById(projectLinkGroup.getProjectGroupId(), 1);
        this.projectLastExecutionRepository.deleteByRef(project.getWorkflowRef());
        this.workflowRepository.deleteByRef(project.getWorkflowRef());
        this.workflowInstanceRepository.deleteByWorkflowRef(project.getWorkflowRef());
        this.asyncTaskInstanceRepository.deleteByWorkflowRef(project.getWorkflowRef());
        this.taskInstanceRepository.deleteByWorkflowRef(project.getWorkflowRef());
        this.gitRepoRepository.deleteById(project.getGitRepoId());
        this.triggerEventRepository.deleteByProjectId(project.getId());

        var eventParameterIds = this.triggerEventRepository.findParameterIdByTriggerIdIn(triggerIds);
        this.triggerEventRepository.deleteParameterByTriggerIdIn(triggerIds);
        this.parameterRepository.deleteByIdIn(eventParameterIds);

        var instanceParameterIds = this.instanceParameterRepository.findParameterIdByTriggerIdIn(triggerIds);
        this.instanceParameterRepository.deleteByTriggerIdIn(triggerIds);
        this.parameterRepository.deleteByIdIn(instanceParameterIds);
    }
}
