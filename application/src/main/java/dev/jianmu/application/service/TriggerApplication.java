package dev.jianmu.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.pagehelper.PageInfo;
import dev.jianmu.application.dsl.webhook.WebhookDslParser;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.exception.IgnoreSyncDslEventException;
import dev.jianmu.application.service.vo.WebhookPayload;
import dev.jianmu.application.util.AssociationUtil;
import dev.jianmu.application.util.ParameterUtil;
import dev.jianmu.el.ElContext;
import dev.jianmu.external_parameter.repository.ExternalParameterRepository;
import dev.jianmu.git.repo.repository.AccessTokenRepository;
import dev.jianmu.git.repo.repository.GitRepoRepository;
import dev.jianmu.infrastructure.GlobalProperties;
import dev.jianmu.infrastructure.mybatis.trigger.WebRequestRepositoryImpl;
import dev.jianmu.infrastructure.quartz.PublishJob;
import dev.jianmu.infrastructure.storage.StorageService;
import dev.jianmu.oauth2.api.config.OAuth2Properties;
import dev.jianmu.oauth2.api.enumeration.ThirdPartyTypeEnum;
import dev.jianmu.oauth2.api.exception.UnknownException;
import dev.jianmu.oauth2.api.impl.OAuth2ApiProxy;
import dev.jianmu.project.repository.ProjectRepository;
import dev.jianmu.secret.aggregate.CredentialManager;
import dev.jianmu.trigger.aggregate.Trigger;
import dev.jianmu.trigger.aggregate.*;
import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookInstance;
import dev.jianmu.trigger.event.CustomWebhookInstanceEvent;
import dev.jianmu.trigger.event.TriggerEvent;
import dev.jianmu.trigger.event.TriggerEventParameter;
import dev.jianmu.trigger.repository.CustomWebhookDefinitionVersionRepository;
import dev.jianmu.trigger.repository.TriggerEventRepository;
import dev.jianmu.trigger.repository.TriggerRepository;
import dev.jianmu.trigger.repository.WebRequestRepository;
import dev.jianmu.trigger.service.CustomWebhookDomainService;
import dev.jianmu.trigger.service.WebhookOnlyService;
import dev.jianmu.workflow.aggregate.parameter.Parameter;
import dev.jianmu.workflow.el.*;
import dev.jianmu.workflow.repository.ParameterRepository;
import dev.jianmu.workflow.repository.WorkflowRepository;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

/**
 * @author Ethan Liu
 * @class TriggerApplication
 * @description TriggerApplication
 * @create 2021-11-10 11:15
 */
@Service
@Slf4j
public class TriggerApplication {
    private final TriggerRepository triggerRepository;
    private final TriggerEventRepository triggerEventRepository;
    private final ParameterRepository parameterRepository;
    private final ProjectRepository projectRepository;
    private final WebRequestRepositoryImpl webRequestRepositoryImpl;
    private final WorkflowRepository workflowRepository;
    private final CredentialManager credentialManager;
    private final Scheduler quartzScheduler;
    private final ApplicationEventPublisher publisher;
    private final ObjectMapper objectMapper;
    // 表达式计算服务
    private final ExpressionLanguage expressionLanguage;
    private final StorageService storageService;
    private final ExternalParameterRepository externalParameterRepository;
    private final OAuth2Properties oAuth2Properties;
    private final GitRepoRepository gitRepoRepository;
    private final CustomWebhookDefinitionVersionRepository webhookDefinitionVersionRepository;
    private final CustomWebhookDomainService customWebhookDomainService;
    private final WebRequestRepository webRequestRepository;
    private final WebhookOnlyService webhookOnlyService;
    private final GlobalProperties globalProperties;
    private final AccessTokenRepository accessTokenRepository;

    public TriggerApplication(
            TriggerRepository triggerRepository,
            TriggerEventRepository triggerEventRepository,
            ParameterRepository parameterRepository,
            ProjectRepository projectRepository,
            WebRequestRepositoryImpl webRequestRepositoryImpl,
            WorkflowRepository workflowRepository,
            CredentialManager credentialManager,
            Scheduler quartzScheduler,
            ApplicationEventPublisher publisher,
            ObjectMapper objectMapper,
            ExpressionLanguage expressionLanguage,
            StorageService storageService,
            ExternalParameterRepository externalParameterRepository,
            OAuth2Properties oAuth2Properties,
            GitRepoRepository gitRepoRepository,
            CustomWebhookDefinitionVersionRepository webhookDefinitionVersionRepository,
            CustomWebhookDomainService customWebhookDomainService,
            WebRequestRepository webRequestRepository,
            WebhookOnlyService webhookOnlyService,
            GlobalProperties globalProperties,
            AccessTokenRepository accessTokenRepository
    ) {
        this.triggerRepository = triggerRepository;
        this.triggerEventRepository = triggerEventRepository;
        this.parameterRepository = parameterRepository;
        this.projectRepository = projectRepository;
        this.webRequestRepositoryImpl = webRequestRepositoryImpl;
        this.workflowRepository = workflowRepository;
        this.credentialManager = credentialManager;
        this.quartzScheduler = quartzScheduler;
        this.publisher = publisher;
        this.objectMapper = objectMapper;
        this.expressionLanguage = expressionLanguage;
        this.storageService = storageService;
        this.externalParameterRepository = externalParameterRepository;
        this.oAuth2Properties = oAuth2Properties;
        this.gitRepoRepository = gitRepoRepository;
        this.webhookDefinitionVersionRepository = webhookDefinitionVersionRepository;
        this.customWebhookDomainService = customWebhookDomainService;
        this.webRequestRepository = webRequestRepository;
        this.webhookOnlyService = webhookOnlyService;
        this.globalProperties = globalProperties;
        this.accessTokenRepository = accessTokenRepository;
    }

    private static String decode(final String encoded) {
        return Optional.ofNullable(encoded)
                .map(e -> URLDecoder.decode(e, StandardCharsets.UTF_8))
                .orElse(null);
    }

    @Transactional
    public void trigger(String triggerId) {
        var trigger = this.triggerRepository.findByTriggerId(triggerId)
                .orElseThrow(() -> new DataNotFoundException("未找到触发器"));
        var project = this.projectRepository.findById(trigger.getProjectId())
                .orElseThrow(() -> new DataNotFoundException("未找到要触发的项目"));
        if (!project.isEnabled()) {
            log.info("当前项目不可触发，请先修改状态");
            return;
        }
        var evt = TriggerEvent.Builder.aTriggerEvent()
                .projectId(trigger.getProjectId())
                .triggerId(trigger.getId())
                .triggerType(trigger.getType().name())
                .build();
        this.triggerEventRepository.save(evt);
        this.publisher.publishEvent(evt);
    }

    @Transactional
    public TriggerEvent trigger(
            List<TriggerEventParameter> eventParameters,
            List<Parameter> parameters,
            WebRequest webRequest
    ) {
        var parametersClean = parameters.stream()
                .filter(parameter -> !(parameter.getType() == Parameter.Type.SECRET))
                .collect(Collectors.toList());
        var event = TriggerEvent.Builder.aTriggerEvent()
                .triggerId(webRequest.getTriggerId())
                .projectId(webRequest.getProjectId())
                .webRequestId(webRequest.getId())
                .payload(webRequest.getPayload())
                .parameters(eventParameters)
                .triggerType(Trigger.Type.WEBHOOK.name())
                .build();
        this.parameterRepository.addAll(parametersClean);
        this.triggerEventRepository.save(event);
        this.publisher.publishEvent(event);
        return event;
    }

    @Transactional
    public void saveOrUpdate(String projectId, String userId, Webhook webhook, String webhookType, List<CustomWebhookInstance.EventInstance> eventInstances) {
        var project = this.projectRepository.findById(projectId)
                .orElseThrow(() -> new DataNotFoundException("未找到项目：" + projectId));
        var webhookInstanceBuilder = CustomWebhookInstanceEvent.Builder.aCustomWebhookInstanceEvent()
                .webhook(webhookType)
                .eventInstances(eventInstances);
        var events = this.customWebhookDomainService.getGitEvents(this.oAuth2Properties.getThirdPartyType(), eventInstances);
        var ref = project.getId();
        var optionalTrigger = this.triggerRepository.findByProjectId(projectId);
        // 修改webhook
        if (optionalTrigger.isPresent()) {
            var trigger = optionalTrigger.get();
            ref = this.updateGitWebhook(trigger.getRef(), ref, userId, project.getAssociationId(), project.getAssociationType(), events);
            trigger.setType(Trigger.Type.WEBHOOK);
            trigger.setWebhook(webhook);
            trigger.setRef(ref);
            trigger.setSchedule(null);
            this.triggerRepository.updateById(trigger);
            // 修改自定义Webhook实例
            if (webhookType != null) {
                this.publisher.publishEvent(webhookInstanceBuilder.triggerId(trigger.getId()).build());
            }
            return;
        }
        // 创建webhook
        ref = this.createGitWebhook(ref, userId, project.getAssociationId(), project.getAssociationType(), events);
        var trigger = Trigger.Builder.aTrigger()
                .ref(ref)
                .projectId(projectId)
                .type(Trigger.Type.WEBHOOK)
                .webhook(webhook)
                .build();
        this.triggerRepository.add(trigger);
        this.triggerRepository.updateById(trigger);
        // 创建CustomWebhookInstance
        if (webhookType != null) {
            this.publisher.publishEvent(webhookInstanceBuilder.triggerId(trigger.getId()).build());
        }
    }

    private String createGitWebhook(String ref, String userId, String associationId, String associationType, List<String> events) {
        if (associationId.isBlank()) {
            return ref;
        }
        ref = URLEncoder.encode(ref, StandardCharsets.UTF_8);
        if (AssociationUtil.AssociationType.GIT_REPO.name().equals(associationType)) {
            var oAuth2Api = OAuth2ApiProxy.builder()
                    .thirdPartyType(ThirdPartyTypeEnum.valueOf(this.oAuth2Properties.getThirdPartyType()))
                    .userId(userId)
                    .build();
            // 创建Git webhook
            var gitRepo = this.gitRepoRepository.findById(associationId)
                    .orElseThrow(() -> new DataNotFoundException("未找到仓库：" + associationId));
            try {
                var accessToken = this.accessTokenRepository.get();
                ref = oAuth2Api.createWebhook(accessToken, gitRepo.getOwner(), gitRepo.getRef(), this.oAuth2Properties.getWebhookHost() + ref, false, events).getId();
                oAuth2Api.updateWebhook(accessToken, gitRepo.getOwner(), gitRepo.getRef(), this.oAuth2Properties.getWebhookHost() + ref, true, ref, events);
            } catch (Exception e) {
                throw new RuntimeException("创建webhook失败：" + e.getMessage());
            }
        }
        return ref;
    }

    private String updateGitWebhook(String ref, String newRef, String userId, String associationId, String associationType, List<String> events) {
        if (associationId.isBlank()) {
            return newRef;
        }
        if (!AssociationUtil.AssociationType.GIT_REPO.name().equals(associationType)) {
            return newRef;
        }
        ref = URLEncoder.encode(ref, StandardCharsets.UTF_8);
        var gitRepo = this.gitRepoRepository.findById(associationId)
                .orElseThrow(() -> new DataNotFoundException("未找到仓库：" + associationId));
        var oAuth2Api = OAuth2ApiProxy.builder()
                .thirdPartyType(ThirdPartyTypeEnum.valueOf(this.oAuth2Properties.getThirdPartyType()))
                .userId(userId)
                .build();
        try {
            var accessToken = this.accessTokenRepository.get();
            try {
                oAuth2Api.getWebhook(accessToken, gitRepo.getOwner(), gitRepo.getRef(), ref);
                oAuth2Api.updateWebhook(accessToken, gitRepo.getOwner(), gitRepo.getRef(), this.oAuth2Properties.getWebhookHost() + ref, true, ref, events);
            } catch (UnknownException e) {
                ref = oAuth2Api.createWebhook(accessToken, gitRepo.getOwner(), gitRepo.getRef(), this.oAuth2Properties.getWebhookHost() + ref, false, events).getId();
                oAuth2Api.updateWebhook(accessToken, gitRepo.getOwner(), gitRepo.getRef(), this.oAuth2Properties.getWebhookHost() + ref, true, ref, events);
            }
            return ref;
        } catch (Exception e) {
            throw new RuntimeException("修改webhook失败：" + e.getMessage());
        }
    }

    @Transactional
    public void saveOrUpdate(String projectId, String schedule) {
        var project = this.projectRepository.findById(projectId)
                .orElseThrow(() -> new DataNotFoundException("未找到项目：" + projectId));
        this.triggerRepository.findByProjectId(projectId)
                .ifPresentOrElse(trigger -> {
                    try {
                        // 更新schedule
                        trigger.setSchedule(schedule);
                        trigger.setType(Trigger.Type.CRON);
                        trigger.setWebhook(null);
                        // 停止触发器
                        this.quartzScheduler.pauseTrigger(TriggerKey.triggerKey(trigger.getId()));
                        // 卸载任务
                        this.quartzScheduler.unscheduleJob(TriggerKey.triggerKey(trigger.getId()));
                        // 删除任务
                        this.quartzScheduler.deleteJob(JobKey.jobKey(trigger.getId()));
                        var jobDetail = this.createJobDetail(trigger);
                        var cronTrigger = this.createCronTrigger(trigger);
                        this.quartzScheduler.scheduleJob(jobDetail, cronTrigger);
                    } catch (SchedulerException e) {
                        log.error("触发器更新失败: {}", e.getMessage());
                        throw new RuntimeException("触发器更新失败");
                    }
                    this.triggerRepository.updateById(trigger);
                }, () -> {
                    var trigger = Trigger.Builder.aTrigger()
                            .projectId(projectId)
                            .type(Trigger.Type.CRON)
                            .schedule(schedule)
                            .build();
                    try {
                        var jobDetail = this.createJobDetail(trigger);
                        var cronTrigger = this.createCronTrigger(trigger);
                        quartzScheduler.scheduleJob(jobDetail, cronTrigger);
                    } catch (SchedulerException e) {
                        log.error("触发器加载失败: {}", e.getMessage());
                        throw new RuntimeException("触发器加载失败");
                    }
                    this.triggerRepository.add(trigger);
                });
    }

    @Transactional
    public void deleteByProjectId(String projectId, String userId, String associationId, String associationType) {
        // 删除Cron触发器
        this.triggerRepository.findByProjectId(projectId)
                .ifPresent(trigger -> {
                    if (trigger.getType() == Trigger.Type.CRON) {
                        try {
                            // 停止触发器
                            quartzScheduler.pauseTrigger(TriggerKey.triggerKey(trigger.getId()));
                            // 卸载任务
                            quartzScheduler.unscheduleJob(TriggerKey.triggerKey(trigger.getId()));
                            // 删除任务
                            quartzScheduler.deleteJob(JobKey.jobKey(trigger.getId()));
                        } catch (SchedulerException e) {
                            log.error("触发器删除失败: {}", e.getMessage());
                            throw new RuntimeException("触发器删除失败");
                        }
                    }
                    this.triggerRepository.deleteById(trigger.getId());
                    this.deleteGitWebhook(userId, associationId, associationType, trigger.getRef());
                });
        // 删除WebRequest
        this.webRequestRepository.deleteByProjectId(projectId);
    }

    // 删除GitWebhook
    private void deleteGitWebhook(String userId, String associationId, String associationType, String ref) {
        if (!AssociationUtil.AssociationType.GIT_REPO.name().equals(associationType)) {
            return;
        }
        var oAuth2Api = OAuth2ApiProxy.builder()
                .thirdPartyType(ThirdPartyTypeEnum.valueOf(this.oAuth2Properties.getThirdPartyType()))
                .userId(userId)
                .build();
        var gitRepo = this.gitRepoRepository.findById(associationId)
                .orElseThrow(() -> new DataNotFoundException("未找到仓库：" + associationId));
        try {
            var accessToken = this.accessTokenRepository.get();
            oAuth2Api.getWebhook(accessToken, gitRepo.getOwner(), gitRepo.getRef(), ref);
            oAuth2Api.deleteWebhook(accessToken, gitRepo.getOwner(), gitRepo.getRef(), ref);
        } catch (UnknownException e) {
            log.info("未找到git webhook: " + ref);
        } catch (Exception e) {
            throw new RuntimeException("删除webhook失败：" + e.getMessage());
        }
    }

    public LocalDateTime getNextFireTime(String projectId) {
        var triggerId = this.triggerRepository.findByProjectId(projectId)
                .filter(trigger -> trigger.getType() == Trigger.Type.CRON)
                .map(Trigger::getId)
                .orElse("");
        if (triggerId.isBlank()) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            var schedulerTrigger = this.quartzScheduler.getTrigger(TriggerKey.triggerKey(triggerId));
            if (schedulerTrigger != null) {
                var dateTime = schedulerTrigger.getNextFireTime()
                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                return dateTime;
            }
            return null;
        } catch (SchedulerException e) {
            log.info("未找到触发器： {}", e.getMessage());
            throw new RuntimeException("未找到触发器");
        }
    }

    public void startTriggers() {
        var triggers = this.triggerRepository.findCronTriggerAll();
        triggers.forEach(trigger -> {
            var cronTrigger = this.createCronTrigger(trigger);
            var jobDetail = this.createJobDetail(trigger);
            try {
                if (this.quartzScheduler.checkExists(jobDetail.getKey())) {
                    return;
                }
                quartzScheduler.scheduleJob(jobDetail, cronTrigger);
            } catch (SchedulerException e) {
                log.error("触发器加载失败: {}", e.getMessage());
                throw new RuntimeException("触发器加载失败");
            }
        });
        try {
            quartzScheduler.start();
        } catch (SchedulerException e) {
            log.error("触发器启动失败: {}", e.getMessage());
            throw new RuntimeException("触发器启动失败");
        }
    }

    private CronTrigger createCronTrigger(Trigger trigger) {
        var builder = CronScheduleBuilder.cronSchedule(trigger.getSchedule());
        return TriggerBuilder.newTrigger()
                .withIdentity(TriggerKey.triggerKey(trigger.getId()))
                .usingJobData("triggerId", trigger.getId())
                .withSchedule(builder)
                .build();
    }

    private JobDetail createJobDetail(Trigger trigger) {
        return JobBuilder.newJob()
                .withIdentity(JobKey.jobKey(trigger.getId()))
                .ofType(PublishJob.class)
                .build();
    }

    public TriggerEvent findTriggerEvent(String triggerEventId) {
        var event = this.triggerEventRepository.findById(triggerEventId)
                .orElseThrow(() -> new DataNotFoundException("未找到该触发事件"));
        if ("WEBHOOK".equals(event.getTriggerType()) && ObjectUtils.isEmpty(event.getPayload())) {
            event.setPayload(this.storageService.readWebhook(event.getWebRequestId()));
        }
        return event;
    }

    public WebhookEvent findWebhookEventByTriggerEvent(TriggerEvent triggerEvent) {
        var webRequest = webRequestRepositoryImpl.findById(triggerEvent.getWebRequestId())
                .orElseThrow(() -> new DataNotFoundException("未找到Webhook请求"));
        var workflow = this.workflowRepository.findByRefAndVersion(webRequest.getWorkflowRef(), webRequest.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义"));
        var project = this.projectRepository.findByWorkflowRef(workflow.getRef())
                .orElseThrow(() -> new DataNotFoundException("未找到该项目"));
        dev.jianmu.application.dsl.webhook.Webhook dslWebhook = WebhookDslParser.parse(workflow.getDslText()).getTrigger();
        if (dslWebhook.getWebhook() == null) {
            return null;
        }
        // 创建表达式上下文
        var context = this.findTriggerContext(triggerEvent.getPayload(), project.getAssociationId(), project.getAssociationType(), project.getAssociationPlatform());
        var webhookDefinitionVersion = this.webhookDefinitionVersionRepository.findByType(dslWebhook.getWebhook())
                .orElseThrow(() -> new DataNotFoundException("未找到Webhook定义版本：" + dslWebhook.getWebhook()));
        // 查询触发事件
        var webhookEvents = this.webhookOnlyService.findEvents(webhookDefinitionVersion.getEvents(), dslWebhook.getEventInstances());
        var webhookEvent = this.findWebhookEvent(webhookEvents, context);
        if (webhookEvent == null) {
            throw new RuntimeException("未找到触发事件");
        }
        // 验证触发规则
        for (WebhookRule webhookRule : webhookEvent.getRuleset()) {
            var res = this.calculateExp(webhookRule.getExpression(), ResultType.BOOL, context);
            if ((Boolean) res.getValue()) {
                webhookRule.succeed();
            }
        }
        return webhookEvent;
    }

    public PageInfo<WebRequest> findWebRequestPage(String projectId, int pageNum, int pageSize) {
        return this.webRequestRepositoryImpl.findPage(projectId, pageNum, pageSize);
    }

    public String getWebhookUrl(String projectId) {
        var trigger = this.triggerRepository.findByProjectId(projectId)
                .orElseThrow(() -> new DataNotFoundException("未找到该项目触发器"));
        return "/webhook/" + trigger.getRef();
    }

    // 获取trigger的context
    private ElContext findTriggerContext(String payload, String associationId, String associationType, String associationPlatform) {
        // 创建表达式上下文
        var context = new ElContext();
        // 外部参数加入上下文
        this.externalParameterRepository.findAll(associationId, associationType, associationPlatform)
                .forEach(extParam -> context.add("ext", extParam.getRef(), ParameterUtil.toParameter(extParam.getType().name(), extParam.getValue())));
        try {
            var webhookPayload = this.objectMapper.readValue(payload, WebhookPayload.class);
            context.add("header", webhookPayload.getHeader());
            context.add("body", webhookPayload.getBody());
            context.add("query", webhookPayload.getQuery());
        } catch (JsonProcessingException e) {
            log.warn("序列化Webhook payload失败");
        }
        return context;
    }

    public void retryHttpEvent(String webRequestId) {
        var webRequest = this.webRequestRepositoryImpl.findById(webRequestId)
                .orElseThrow(() -> new DataNotFoundException("未找到可重试的记录"));
        var newWebRequest = WebRequest.Builder.aWebRequest()
                .payload(this.storageService.readWebhook(webRequest.getId()))
                .userAgent(webRequest.getUserAgent())
                .statusCode(WebRequest.StatusCode.OK)
                .build();
        this.writeWebhook(newWebRequest.getId(), newWebRequest.getPayload());
        var project = this.projectRepository.findById(webRequest.getProjectId())
                .orElseThrow(() -> {
                    newWebRequest.setStatusCode(WebRequest.StatusCode.NOT_FOUND);
                    newWebRequest.setErrorMsg("未找到项目ID: " + webRequest.getProjectId());
                    this.webRequestRepositoryImpl.add(newWebRequest);
                    return new DataNotFoundException("未找到项目ID: " + webRequest.getProjectId());
                });
        if (!project.isEnabled()) {
            throw new RuntimeException("当前项目不可触发，请先修改状态");
        }
        newWebRequest.setProjectId(project.getId());
        newWebRequest.setWorkflowRef(project.getWorkflowRef());
        newWebRequest.setWorkflowVersion(project.getWorkflowVersion());
        var trigger = this.triggerRepository.findByProjectId(project.getId())
                .orElseThrow(() -> {
                    newWebRequest.setStatusCode(WebRequest.StatusCode.NOT_FOUND);
                    newWebRequest.setErrorMsg("项目：" + project.getWorkflowName() + " 未找到触发器");
                    this.webRequestRepositoryImpl.add(newWebRequest);
                    return new DataNotFoundException("项目：" + project.getWorkflowName() + " 未找到触发器");
                });
        newWebRequest.setTriggerId(trigger.getId());
        if (trigger.getType() != Trigger.Type.WEBHOOK) {
            newWebRequest.setStatusCode(WebRequest.StatusCode.NOT_ACCEPTABLE);
            newWebRequest.setErrorMsg("项目：" + project.getWorkflowName() + " 未找到触发器");
            this.webRequestRepositoryImpl.add(newWebRequest);
            throw new IllegalArgumentException("项目：" + project.getWorkflowName() + "触发器类型错误");
        }
        var webhook = trigger.getWebhook();
        var webhookParams = webhook.getParam();
        // 创建表达式上下文
        var context = this.findTriggerContext(newWebRequest.getPayload(), project.getAssociationId(), project.getAssociationType(), project.getAssociationPlatform());
        // 查询触发事件
        WebhookEvent webhookEvent = null;
        if (webhook.getEvents() != null) {
            webhookEvent = this.findWebhookEvent(trigger.getWebhook().getEvents(), context);
            if (webhookEvent == null) {
                newWebRequest.setStatusCode(WebRequest.StatusCode.NOT_FOUND);
                newWebRequest.setErrorMsg("未找到触发器事件");
                this.webRequestRepositoryImpl.add(newWebRequest);
                throw new IllegalArgumentException("项目：" + project.getWorkflowName() + "，未找到触发器事件");
            }
            webhookParams = webhookEvent.getAvailableParams();
        }
        // 提取参数
        List<TriggerEventParameter> eventParameters = new ArrayList<>();
        List<Parameter> parameters = new ArrayList<>();
        if (webhookParams != null) {
            for (WebhookParameter webhookParameter : webhookParams) {
                var value = this.extractParameter(context, webhookParameter);
                if (value == null && webhookParameter.getRequired()) {
                    newWebRequest.setStatusCode(WebRequest.StatusCode.PARAMETER_WAS_NULL);
                    newWebRequest.setErrorMsg("未找到Webhook参数：" + webhookParameter.getName());
                    this.webRequestRepositoryImpl.add(newWebRequest);
                    throw new IllegalArgumentException("项目：" + project.getWorkflowName() + "，未找到Webhook参数：" + webhookParameter.getName());
                }
                if (value == null) {
                    continue;
                }
                Parameter<?> parameter = Parameter.Type
                        .getTypeByName(webhookParameter.getType())
                        .newParameter(value);
                var eventParameter = TriggerEventParameter.Builder.aTriggerParameter()
                        .ref(webhookParameter.getRef())
                        .name(webhookParameter.getName())
                        .type(webhookParameter.getType())
                        .value(parameter.getStringValue())
                        .required(webhookParameter.getRequired())
                        .hidden(webhookParameter.getHidden())
                        .parameterId(parameter.getId())
                        .build();
                parameters.add(parameter);
                eventParameters.add(eventParameter);
                context.add("trigger", eventParameter.getName(), parameter);
            }
        }
        // 验证触发规则
        if (webhookEvent != null) {
            boolean succeed = false;
            for (WebhookRule webhookRule : webhookEvent.getRuleset()) {
                var res = this.calculateExp(webhookRule.getExpression(), ResultType.BOOL, context);
                var bool = (Boolean) res.getValue();
                succeed = bool;
                if (!bool && webhookEvent.getRulesetOperator() == CustomWebhookInstance.RulesetOperator.AND) {
                    var message = "不满足：" + webhookRule.getParamName() + webhookRule.getOperator().name + webhookRule.getMatchingValue();
                    log.warn(message);
                    newWebRequest.setStatusCode(WebRequest.StatusCode.NOT_ACCEPTABLE);
                    newWebRequest.setErrorMsg(message);
                    this.webRequestRepositoryImpl.add(newWebRequest);
                    throw new RuntimeException(message);
                }
                if (bool && webhookEvent.getRulesetOperator() == CustomWebhookInstance.RulesetOperator.OR) {
                    break;
                }
            }
            if (!succeed && webhookEvent.getRulesetOperator() == CustomWebhookInstance.RulesetOperator.OR) {
                log.warn("未满足任何一条触发规则");
                newWebRequest.setStatusCode(WebRequest.StatusCode.NOT_ACCEPTABLE);
                newWebRequest.setErrorMsg("未满足任何一条触发规则");
                this.webRequestRepositoryImpl.add(newWebRequest);
                throw new RuntimeException("未满足任何一条触发规则");
            }
        }
        // 验证Auth
        if (webhook.getAuth() != null) {
            var auth = webhook.getAuth();
            var authToken = this.calculateExp(auth.getToken(), ResultType.STRING, context);
            var authValue = this.findSecret(auth.getValue(), project.getAssociationId(), project.getAssociationType(), project.getAssociationPlatform());
            if (authToken.getType() != Parameter.Type.STRING) {
                log.warn("Auth Token表达式计算错误");
                newWebRequest.setStatusCode(WebRequest.StatusCode.UNAUTHORIZED);
                newWebRequest.setErrorMsg("Auth Token表达式计算错误");
                this.webRequestRepositoryImpl.add(newWebRequest);
                return;
            }
            if (!authToken.getValue().equals(authValue)) {
                log.warn("Webhook密钥不匹配");
                newWebRequest.setStatusCode(WebRequest.StatusCode.UNAUTHORIZED);
                newWebRequest.setErrorMsg("Webhook密钥不匹配");
                this.webRequestRepositoryImpl.add(newWebRequest);
                return;
            }
        }
        // 验证Matcher
        if (webhook.getOnly() != null) {
            var res = this.calculateExp(webhook.getOnly(), ResultType.BOOL, context);
            if (res.getType() != Parameter.Type.BOOL || !((Boolean) res.getValue())) {
                log.warn("Only计算不匹配，计算结果为：{}", res.getStringValue());
                newWebRequest.setStatusCode(WebRequest.StatusCode.NOT_ACCEPTABLE);
                newWebRequest.setErrorMsg("Only计算不匹配，计算结果为：" + res.getStringValue());
                this.webRequestRepositoryImpl.add(newWebRequest);
                return;
            }
        }
        this.webRequestRepositoryImpl.add(newWebRequest);
        this.trigger(eventParameters, parameters, newWebRequest);
    }

    private WebhookEvent findWebhookEvent(List<WebhookEvent> events, ElContext context) {
        return events.stream()
                .filter(event -> {
                    var currentContext = context.copy();
                    for (WebhookParameter webhookParameter : event.getEventParams()) {
                        var value = this.extractParameter(currentContext, webhookParameter);
                        if (value == null) {
                            return false;
                        }
                        Parameter<?> parameter = Parameter.Type
                                .getTypeByName(webhookParameter.getType())
                                .newParameter(value);
                        var eventParameter = TriggerEventParameter.Builder.aTriggerParameter()
                                .ref(webhookParameter.getRef())
                                .name(webhookParameter.getName())
                                .type(webhookParameter.getType())
                                .value(parameter.getStringValue())
                                .required(webhookParameter.getRequired())
                                .hidden(webhookParameter.getHidden())
                                .parameterId(parameter.getId())
                                .build();
                        currentContext.add("trigger", eventParameter.getName(), parameter);
                    }
                    var res = this.calculateExp(event.getOnly(), ResultType.BOOL, context);
                    return res.getType() == Parameter.Type.BOOL && (Boolean) res.getValue();
                })
                .findFirst()
                .orElse(null);
    }

    public TriggerEvent receiveHttpEvent(String ref, HttpServletRequest request, String contentType) {
        var webRequest = this.createWebRequest(request, contentType);
        this.writeWebhook(webRequest.getId(), webRequest.getPayload());
        var trigger = this.triggerRepository.findByRef(ref)
                .orElseThrow(() -> {
                    webRequest.setStatusCode(WebRequest.StatusCode.NOT_FOUND);
                    webRequest.setErrorMsg("未找到触发器：" + ref);
                    this.webRequestRepositoryImpl.add(webRequest);
                    return new DataNotFoundException("未找到触发器：" + ref);
                });
        if (trigger.getType() != Trigger.Type.WEBHOOK) {
            webRequest.setStatusCode(WebRequest.StatusCode.NOT_ACCEPTABLE);
            webRequest.setErrorMsg("未找到触发器");
            this.webRequestRepositoryImpl.add(webRequest);
            throw new IllegalArgumentException("触发器类型错误");
        }
        var project = this.projectRepository.findById(trigger.getProjectId())
                .orElseThrow(() -> new DataNotFoundException("未找到项目"));
        if (!project.isEnabled()) {
            throw new RuntimeException("当前项目不可触发，请先修改状态");
        }
        webRequest.setProjectId(project.getId());
        webRequest.setWorkflowRef(project.getWorkflowRef());
        webRequest.setWorkflowVersion(project.getWorkflowVersion());
        webRequest.setTriggerId(trigger.getId());
        var webhook = trigger.getWebhook();
        var webhookParams = webhook.getParam();
        // 创建表达式上下文
        var context = this.findTriggerContext(webRequest.getPayload(), project.getAssociationId(), project.getAssociationType(), project.getAssociationPlatform());
        // 查询触发事件
        WebhookEvent webhookEvent = null;
        if (webhook.getEvents() != null) {
            webhookEvent = this.findWebhookEvent(trigger.getWebhook().getEvents(), context);
            if (webhookEvent == null) {
                webRequest.setStatusCode(WebRequest.StatusCode.NOT_FOUND);
                webRequest.setErrorMsg("未找到触发器事件");
                this.webRequestRepositoryImpl.add(webRequest);
                throw new IllegalArgumentException("项目：" + project.getWorkflowName() + "，未找到触发器事件");
            }
            webhookParams = webhookEvent.getAvailableParams();
        }
        // 提取参数
        List<TriggerEventParameter> eventParameters = new ArrayList<>();
        List<Parameter> parameters = new ArrayList<>();
        if (webhookParams != null) {
            for (WebhookParameter webhookParameter : webhookParams) {
                var value = this.extractParameter(context, webhookParameter);
                if (value == null && webhookParameter.getRequired()) {
                    webRequest.setStatusCode(WebRequest.StatusCode.PARAMETER_WAS_NULL);
                    webRequest.setErrorMsg("未找到Webhook参数：" + webhookParameter.getName());
                    this.webRequestRepositoryImpl.add(webRequest);
                    throw new IllegalArgumentException("项目：" + project.getWorkflowName() + "，未找到Webhook参数：" + webhookParameter.getName());
                }
                if (value == null) {
                    continue;
                }
                Parameter<?> parameter = Parameter.Type
                        .getTypeByName(webhookParameter.getType())
                        .newParameter(value);
                var eventParameter = TriggerEventParameter.Builder.aTriggerParameter()
                        .ref(webhookParameter.getRef())
                        .name(webhookParameter.getName())
                        .type(webhookParameter.getType())
                        .value(parameter.getStringValue())
                        .required(webhookParameter.getRequired())
                        .hidden(webhookParameter.getHidden())
                        .parameterId(parameter.getId())
                        .build();
                parameters.add(parameter);
                eventParameters.add(eventParameter);
                context.add("trigger", eventParameter.getName(), parameter);
            }
        }
        // 验证触发规则
        if (webhookEvent != null) {
            boolean succeed = false;
            for (WebhookRule webhookRule : webhookEvent.getRuleset()) {
                var res = this.calculateExp(webhookRule.getExpression(), ResultType.BOOL, context);
                var bool = (Boolean) res.getValue();
                succeed = bool;
                if (!bool && webhookEvent.getRulesetOperator() == CustomWebhookInstance.RulesetOperator.AND) {
                    var message = "不满足：" + webhookRule.getParamName() + webhookRule.getOperator().name + webhookRule.getMatchingValue();
                    log.warn(message);
                    webRequest.setStatusCode(WebRequest.StatusCode.NOT_ACCEPTABLE);
                    webRequest.setErrorMsg(message);
                    this.webRequestRepositoryImpl.add(webRequest);
                    throw new RuntimeException(message);
                }
                if (bool && webhookEvent.getRulesetOperator() == CustomWebhookInstance.RulesetOperator.OR) {
                    break;
                }
            }
            if (!succeed && webhookEvent.getRulesetOperator() == CustomWebhookInstance.RulesetOperator.OR) {
                log.warn("未满足任何一条触发规则");
                webRequest.setStatusCode(WebRequest.StatusCode.NOT_ACCEPTABLE);
                webRequest.setErrorMsg("未满足任何一条触发规则");
                this.webRequestRepositoryImpl.add(webRequest);
                throw new RuntimeException("未满足任何一条触发规则");
            }
        }
        // 验证Auth
        if (webhook.getAuth() != null) {
            var auth = webhook.getAuth();
            var authToken = this.calculateExp(auth.getToken(), ResultType.STRING, context);
            var authValue = this.findSecret(auth.getValue(), project.getAssociationId(), project.getAssociationType(), project.getAssociationPlatform());
            if (authToken.getType() != Parameter.Type.STRING) {
                log.warn("Auth Token表达式计算错误");
                webRequest.setStatusCode(WebRequest.StatusCode.UNAUTHORIZED);
                webRequest.setErrorMsg("Auth Token表达式计算错误");
                this.webRequestRepositoryImpl.add(webRequest);
                throw new RuntimeException("Auth Token表达式计算错误");
            }
            if (!authToken.getValue().equals(authValue)) {
                log.warn("Webhook密钥不匹配");
                webRequest.setStatusCode(WebRequest.StatusCode.UNAUTHORIZED);
                webRequest.setErrorMsg("Webhook密钥不匹配");
                this.webRequestRepositoryImpl.add(webRequest);
                throw new RuntimeException("Webhook密钥不匹配");
            }
        }
        // 验证Matcher
        if (webhook.getOnly() != null) {
            var res = this.calculateExp(webhook.getOnly(), ResultType.BOOL, context);
            if (res.getType() != Parameter.Type.BOOL || !((Boolean) res.getValue())) {
                log.warn("Only计算不匹配，计算结果为：{}", res.getStringValue());
                webRequest.setStatusCode(WebRequest.StatusCode.NOT_ACCEPTABLE);
                webRequest.setErrorMsg("Only计算不匹配，计算结果为：" + res.getStringValue());
                this.webRequestRepositoryImpl.add(webRequest);
                throw new RuntimeException("Only计算不匹配，计算结果为：" + res.getStringValue());
            }
        }
        this.webRequestRepositoryImpl.add(webRequest);
        return this.trigger(eventParameters, parameters, webRequest);
    }

    private void writeWebhook(String webhookRequestId, String payload) {
        try (var webhookWriter = this.storageService.writeWebhook(webhookRequestId)) {
            webhookWriter.write(payload);
            webhookWriter.flush();
        } catch (IOException e) {
            log.error("写入webhook文件异常:", e);
        }
    }

    private String findSecret(String secretExp, String associationId, String associationType, String associationPlatform) {
        var secret = this.isSecret(secretExp);
        if (secret == null) {
            throw new IllegalArgumentException("密钥参数格式错误：" + secretExp);
        }
        // 处理密钥类型参数, 获取值后转换为String类型参数
        var strings = secret.split("\\.");
        var kv = this.credentialManager.findByNamespaceNameAndKey(strings[0], strings[1], associationId, associationType, associationPlatform)
                .orElseThrow(() -> new DataNotFoundException("未找到密钥"));
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

    private Parameter<?> calculateExp(String exp, ResultType resultType, EvaluationContext context) {
        // 密钥类型单独处理
        var secret = this.isSecret(exp);
        if (secret != null) {
            return Parameter.Type.SECRET.newParameter(secret);
        }
        // 计算参数表达式
        Expression expression = expressionLanguage.parseExpression(exp, resultType);
        EvaluationResult evaluationResult = expressionLanguage.evaluateExpression(expression, context);
        if (evaluationResult.isFailure()) {
            var errorMsg = "表达式：" + exp +
                    " 计算错误: " + evaluationResult.getFailureMessage();
            throw new RuntimeException(errorMsg);
        }
        return evaluationResult.getValue();
    }

    private Object extractParameter(ElContext context, WebhookParameter webhookParameter) {
        try {
            var expression = this.expressionLanguage.parseExpression(webhookParameter.getValue().toString(), ResultType.valueOf(webhookParameter.getType()));
            var result = this.expressionLanguage.evaluateExpression(expression, context);
            if (result.isFailure()) {
                log.warn("触发器参数: {} 表达式: {} 计算错误: {}", webhookParameter.getRef(), expression.getExpression(), result.getFailureMessage());
                return null;
            }
            return result.getValue().getValue();
        } catch (Exception e) {
            return null;
        }
    }

    private WebRequest createWebRequest(HttpServletRequest request, String contentType) {
        try {
            // Get body
            var body = request.getReader()
                    .lines()
                    .collect(Collectors.joining(System.lineSeparator()));
            // Create root node
            var root = this.objectMapper.createObjectNode();
            // Headers node
            Map<String, List<String>> headers = Collections.list(request.getHeaderNames())
                    .stream()
                    .collect(Collectors.toMap(
                            Function.identity(),
                            h -> Collections.list(request.getHeaders(h))
                    ));
            var headerNode = root.putObject("header");
            headers.forEach((key, value) -> {
                if (value.size() > 1) {
                    var item = headerNode.putArray(key);
                    value.forEach(item::add);
                } else {
                    headerNode.put(key, value.get(0));
                }
            });
            // Query String node
            var url = request.getRequestURL().toString();
            var queryString = request.getQueryString();
            MultiValueMap<String, String> parameters =
                    UriComponentsBuilder.fromUriString(url + "?" + queryString).build().getQueryParams();
            var queryNode = root.putObject("query");
            parameters.forEach((key, value) -> {
                if ("null".equals(key)) {
                    return;
                }
                if (value.size() > 1) {
                    var item = queryNode.putArray(key);
                    value.forEach(item::add);
                } else {
                    queryNode.put(key, value.get(0));
                }
            });
            // Body node
            var bodyNode = root.putObject("body");
            // Body Json node
            if (contentType.startsWith("application/json")) {
                var bodyJson = this.objectMapper.readTree(body);
                bodyNode.set("json", bodyJson);
                // 忽略同步流水线DSl引发的push事件
                this.ignoreSyncDslEvent(headerNode, bodyJson);
            }
            // Body Form node
            if (contentType.startsWith("application/x-www-form-urlencoded")) {
                var formNode = bodyNode.putObject("form");
                var formMap = Pattern.compile("&")
                        .splitAsStream(body)
                        .map(s -> Arrays.copyOf(s.split("=", 2), 2))
                        .collect(groupingBy(s -> decode(s[0]), mapping(s -> decode(s[1]), toList())));
                formMap.forEach((key, value) -> {
                    if (value.size() > 1) {
                        var item = formNode.putArray(key);
                        value.forEach(item::add);
                    } else {
                        formNode.put(key, value.get(0));
                    }
                });
            }
            // Body Text node
            if (contentType.startsWith("text/plain")) {
                bodyNode.put("text", body);
            }
            return WebRequest.Builder.aWebRequest()
                    .userAgent(request.getHeader("User-Agent"))
                    .payload(root.toString())
                    .statusCode(WebRequest.StatusCode.OK)
                    .build();
        } catch (IgnoreSyncDslEventException e) {
            throw new IgnoreSyncDslEventException(e.getMessage());
        } catch (Exception e) {
            return WebRequest.Builder.aWebRequest()
                    .userAgent(request.getHeader("User-Agent"))
                    .statusCode(WebRequest.StatusCode.UNKNOWN)
                    .errorMsg(e.getMessage())
                    .build();
        }
    }

    private void ignoreSyncDslEvent(ObjectNode header, JsonNode body) {
        if (!this.oAuth2Properties.getThirdPartyType().equals(ThirdPartyTypeEnum.GITLINK.name())) {
            return;
        }
        try {
            if (!"push".equals(header.get("x-gitea-event").asText())) {
                return;
            }
            var author = body.get("head_commit").get("committer");
            if (!ProjectApplication.committer.equals(author.get("name").asText())) {
                return;
            }
            if (!ProjectApplication.committerEmail.equals(author.get("email").asText())) {
                return;
            }
            throw new IgnoreSyncDslEventException("忽略同步流水线DSl所触发的push事件");
        } catch (IgnoreSyncDslEventException e) {
            throw new IgnoreSyncDslEventException(e.getMessage());
        } catch (Exception ignored) {
        }
    }

    public dev.jianmu.application.dsl.webhook.Webhook getWebhookParam(String id) {
        var webRequest = webRequestRepositoryImpl.findById(id)
                .orElseThrow(() -> new DataNotFoundException("未找到Webhook请求"));
        var workflow = this.workflowRepository.findByRefAndVersion(webRequest.getWorkflowRef(), webRequest.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义"));
        var project = this.projectRepository.findByWorkflowRef(workflow.getRef())
                .orElseThrow(() -> new DataNotFoundException("未找到该项目"));
        if (ObjectUtils.isEmpty(webRequest.getPayload())) {
            webRequest.setPayload(this.storageService.readWebhook(webRequest.getId()));
        }
        // 创建表达式上下文
        var context = this.findTriggerContext(webRequest.getPayload(), project.getAssociationId(), project.getAssociationType(), project.getAssociationPlatform());
        WebhookEvent webhookEvent = null;
        dev.jianmu.application.dsl.webhook.Webhook trigger = WebhookDslParser.parse(workflow.getDslText()).getTrigger();
        if (trigger.getWebhook() != null) {
            var webhookDefinitionVersion = this.webhookDefinitionVersionRepository.findByType(trigger.getWebhook())
                    .orElseThrow(() -> new DataNotFoundException("未找到Webhook定义版本：" + trigger.getWebhook()));
            // 查询触发事件
            var webhookEvents = this.webhookOnlyService.findEvents(webhookDefinitionVersion.getEvents(), trigger.getEventInstances());
            webhookEvent = this.findWebhookEvent(webhookEvents, context);
            if (webhookEvent == null) {
                throw new RuntimeException("未找到触发事件");
            }
            trigger.setParam(webhookEvent.getAvailableParams());
            trigger.setWebhookEvent(webhookEvent);
        }
        if (trigger.getParam() == null) {
            return trigger;
        }
        // 添加触发器参数
        var parameters = new ArrayList<WebhookParameter>();
        for (WebhookParameter webhookParameter : trigger.getParam()) {
            var newParam = WebhookParameter.Builder.aWebhookParameter()
                    .ref(webhookParameter.getRef())
                    .name(webhookParameter.getName() == null ? webhookParameter.getRef() : webhookParameter.getName())
                    .value(webhookParameter.getValue())
                    .type(webhookParameter.getType() == null ? Parameter.Type.STRING.name() : webhookParameter.getType())
                    .required(webhookParameter.getRequired() != null && webhookParameter.getRequired())
                    .hidden(webhookParameter.getHidden() != null && webhookParameter.getHidden())
                    .build();
            var value = this.extractParameter(context, newParam);
            if (value != null) {
                newParam.setValue(newParam.getHidden() ? "**********" : value);
                parameters.add(newParam);
            }
        }
        trigger.setParam(parameters);
        // 验证触发规则
        if (webhookEvent != null) {
            for (WebhookRule webhookRule : webhookEvent.getRuleset()) {
                var res = this.calculateExp(webhookRule.getExpression(), ResultType.BOOL, context);
                if ((Boolean) res.getValue()) {
                    webhookRule.succeed();
                }
            }
        }
        return trigger;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateTriggerStatus(String triggerId, String triggerType) {
        if (!triggerType.equals(Trigger.Type.WEBHOOK.name())) {
            return;
        }
        var triggerEvent = this.triggerEventRepository.findById(triggerId)
                .orElseThrow(() -> new DataNotFoundException("未找到触发器"));
        var webRequest = this.webRequestRepositoryImpl.findById(triggerEvent.getWebRequestId())
                .orElseThrow(() -> new DataNotFoundException("未找到Webhook请求"));
        webRequest.setStatusCode(WebRequest.StatusCode.ALREADY_RUNNING);
        webRequest.setErrorMsg("待执行流程数已超过最大值：" + this.globalProperties.getTriggerQueue().getMax());
        this.webRequestRepositoryImpl.update(webRequest);
    }

    public Optional<WebRequest> findWebRequestById(String webRequestId) {
        return this.webRequestRepositoryImpl.findById(webRequestId);
    }
}
