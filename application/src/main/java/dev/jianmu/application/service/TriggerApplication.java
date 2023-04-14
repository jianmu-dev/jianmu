package dev.jianmu.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import dev.jianmu.application.dsl.webhook.WebhookDslParser;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.el.ElContext;
import dev.jianmu.infrastructure.mybatis.trigger.WebRequestRepositoryImpl;
import dev.jianmu.infrastructure.quartz.PublishJob;
import dev.jianmu.infrastructure.storage.StorageService;
import dev.jianmu.project.repository.ProjectRepository;
import dev.jianmu.secret.aggregate.CredentialManager;
import dev.jianmu.trigger.aggregate.Trigger;
import dev.jianmu.trigger.aggregate.WebRequest;
import dev.jianmu.trigger.aggregate.Webhook;
import dev.jianmu.trigger.event.TriggerEvent;
import dev.jianmu.trigger.event.TriggerEventParameter;
import dev.jianmu.trigger.repository.TriggerEventRepository;
import dev.jianmu.trigger.repository.TriggerRepository;
import dev.jianmu.workflow.aggregate.parameter.Parameter;
import dev.jianmu.workflow.el.EvaluationContext;
import dev.jianmu.workflow.el.EvaluationResult;
import dev.jianmu.workflow.el.Expression;
import dev.jianmu.workflow.el.ExpressionLanguage;
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
            StorageService storageService) {
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
        // 过滤SECRET类型参数不保存
        var eventParametersClean = eventParameters.stream()
                .filter(triggerEventParameter -> !triggerEventParameter.getType().equals("SECRET"))
                .collect(Collectors.toList());
        var parametersClean = parameters.stream()
                .filter(parameter -> !(parameter.getType() == Parameter.Type.SECRET))
                .collect(Collectors.toList());
        var event = TriggerEvent.Builder.aTriggerEvent()
                .triggerId(webRequest.getTriggerId())
                .projectId(webRequest.getProjectId())
                .webRequestId(webRequest.getId())
                .payload(webRequest.getPayload())
                .parameters(eventParametersClean)
                .triggerType(Trigger.Type.WEBHOOK.name())
                .build();
        this.parameterRepository.addAll(parametersClean);
        this.triggerEventRepository.save(event);
        this.publisher.publishEvent(event);
        return event;
    }

    @Transactional
    public void saveOrUpdate(String projectId, Webhook webhook) {
        this.triggerRepository.findByProjectId(projectId)
                .ifPresentOrElse(trigger -> {
                    trigger.setType(Trigger.Type.WEBHOOK);
                    trigger.setWebhook(webhook);
                    trigger.setSchedule(null);
                    this.triggerRepository.updateById(trigger);
                }, () -> {
                    var trigger = Trigger.Builder.aTrigger()
                            .projectId(projectId)
                            .type(Trigger.Type.WEBHOOK)
                            .webhook(webhook)
                            .build();
                    this.triggerRepository.add(trigger);
                });
    }

    @Transactional
    public void saveOrUpdate(String projectId, String schedule) {
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
    public void deleteByProjectId(String projectId) {
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
                });
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

    public PageInfo<WebRequest> findWebRequestPage(String projectId, int pageNum, int pageSize) {
        return this.webRequestRepositoryImpl.findPage(projectId, pageNum, pageSize);
    }

    public String getWebhookUrl(String projectId) {
        var project = this.projectRepository.findById(projectId)
                .orElseThrow(() -> new DataNotFoundException("未找到该项目"));
        return "/webhook/" + URLEncoder.encode(project.getWorkflowName(), StandardCharsets.UTF_8);
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
        // 创建表达式上下文
        var context = new ElContext();
        // 提取参数
        var webhook = trigger.getWebhook();
        List<TriggerEventParameter> eventParameters = new ArrayList<>();
        List<Parameter> parameters = new ArrayList<>();
        if (webhook.getParam() != null) {
            webhook.getParam().forEach(webhookParameter -> {
                var value = this.extractParameter(newWebRequest.getPayload(), webhookParameter.getExp(), webhookParameter.getType());
                if (value == null && webhookParameter.isRequired()) {
                    newWebRequest.setStatusCode(WebRequest.StatusCode.PARAMETER_WAS_NULL);
                    newWebRequest.setErrorMsg("触发器参数" + webhookParameter.getName() + "的值为null");
                    this.webRequestRepositoryImpl.add(newWebRequest);
                    throw new IllegalArgumentException("项目：" + project.getWorkflowName() + " 触发器参数" + webhookParameter.getName() + "的值为null");
                }
                Parameter<?> parameter = Parameter.Type
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
                context.add("trigger", eventParameter.getName(), parameter);
            });
        }
        // 验证Auth
        if (webhook.getAuth() != null) {
            var auth = webhook.getAuth();
            var authToken = this.calculateExp(auth.getToken(), context);
            var authValue = this.findSecret(auth.getValue());
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
            var res = this.calculateExp(webhook.getOnly(), context);
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

    public TriggerEvent receiveHttpEvent(String projectName, HttpServletRequest request, String contentType) {
        var webRequest = this.createWebRequest(request, contentType);
        this.writeWebhook(webRequest.getId(), webRequest.getPayload());
        var project = this.projectRepository.findByName(projectName)
                .orElseThrow(() -> {
                    webRequest.setStatusCode(WebRequest.StatusCode.NOT_FOUND);
                    webRequest.setErrorMsg("未找到项目: " + projectName);
                    this.webRequestRepositoryImpl.add(webRequest);
                    return new DataNotFoundException("未找到项目: " + projectName);
                });
        if (!project.isEnabled()) {
            throw new RuntimeException("当前项目不可触发，请先修改状态");
        }
        webRequest.setProjectId(project.getId());
        webRequest.setWorkflowRef(project.getWorkflowRef());
        webRequest.setWorkflowVersion(project.getWorkflowVersion());
        var trigger = this.triggerRepository.findByProjectId(project.getId())
                .orElseThrow(() -> {
                    webRequest.setStatusCode(WebRequest.StatusCode.NOT_FOUND);
                    webRequest.setErrorMsg("项目：" + projectName + " 未找到触发器");
                    this.webRequestRepositoryImpl.add(webRequest);
                    return new DataNotFoundException("项目：" + projectName + " 未找到触发器");
                });
        webRequest.setTriggerId(trigger.getId());
        if (trigger.getType() != Trigger.Type.WEBHOOK) {
            webRequest.setStatusCode(WebRequest.StatusCode.NOT_ACCEPTABLE);
            webRequest.setErrorMsg("项目：" + projectName + " 未找到触发器");
            this.webRequestRepositoryImpl.add(webRequest);
            throw new IllegalArgumentException("项目：" + projectName + "触发器类型错误");
        }
        // 创建表达式上下文
        var context = new ElContext();
        // 提取参数
        var webhook = trigger.getWebhook();
        List<TriggerEventParameter> eventParameters = new ArrayList<>();
        List<Parameter> parameters = new ArrayList<>();
        if (webhook.getParam() != null) {
            webhook.getParam().forEach(webhookParameter -> {
                var value = this.extractParameter(webRequest.getPayload(), webhookParameter.getExp(), webhookParameter.getType());
                if (value == null && webhookParameter.isRequired()) {
                    webRequest.setStatusCode(WebRequest.StatusCode.PARAMETER_WAS_NULL);
                    webRequest.setErrorMsg("未找到触发器参数：" + webhookParameter.getName());
                    this.webRequestRepositoryImpl.add(webRequest);
                    throw new IllegalArgumentException("未找到触发器参数：" + webhookParameter.getName());
                }
                Parameter<?> parameter = Parameter.Type
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
                context.add("trigger", eventParameter.getName(), parameter);
            });
        }
        // 验证Auth
        if (webhook.getAuth() != null) {
            var auth = webhook.getAuth();
            var authToken = this.calculateExp(auth.getToken(), context);
            var authValue = this.findSecret(auth.getValue());
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
            var res = this.calculateExp(webhook.getOnly(), context);
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

    private String findSecret(String secretExp) {
        var secret = this.isSecret(secretExp);
        if (secret == null) {
            throw new IllegalArgumentException("密钥参数格式错误：" + secretExp);
        }
        // 处理密钥类型参数, 获取值后转换为String类型参数
        var strings = secret.split("\\.");
        var kv = this.credentialManager.findByNamespaceNameAndKey(strings[0], strings[1])
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

    private Parameter<?> calculateExp(String exp, EvaluationContext context) {
        // 密钥类型单独处理
        var secret = this.isSecret(exp);
        if (secret != null) {
            return Parameter.Type.SECRET.newParameter(secret);
        }
        String el;
        if (isEl(exp)) {
            el = exp;
        } else {
            el = "`" + exp + "`";
        }
        // 计算参数表达式
        Expression expression = expressionLanguage.parseExpression(el);
        EvaluationResult evaluationResult = expressionLanguage.evaluateExpression(expression, context);
        if (evaluationResult.isFailure()) {
            var errorMsg = "表达式：" + exp +
                    " 计算错误: " + evaluationResult.getFailureMessage();
            throw new RuntimeException(errorMsg);
        }
        return evaluationResult.getValue();
    }

    private boolean isEl(String paramValue) {
        Pattern pattern = Pattern.compile("^\\(");
        Matcher matcher = pattern.matcher(paramValue);
        return matcher.lookingAt();
    }

    private Object extractParameter(String payload, String exp, String webhookType) {
        if (exp.startsWith("$.header.")) {
            exp = exp.toLowerCase(Locale.ROOT);
        }
        Object document = Configuration.defaultConfiguration()
                .jsonProvider().parse(payload);
        try {
            var conf = Configuration.defaultConfiguration()
                    .addOptions(Option.ALWAYS_RETURN_LIST, Option.DEFAULT_PATH_LEAF_TO_NULL);
            List<?> vars = JsonPath.using(conf).parse(document).read(exp);
            if (vars.isEmpty()) {
                return null;
            }
            var type = Parameter.Type.getTypeByName(webhookType);
            if (type == Parameter.Type.SECRET || type == Parameter.Type.STRING) {
                return vars.get(0) == null ? null : vars.get(0).toString().trim();
            }
            return vars.get(0);
        } catch (PathNotFoundException e) {
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
        } catch (Exception e) {
            return WebRequest.Builder.aWebRequest()
                    .userAgent(request.getHeader("User-Agent"))
                    .statusCode(WebRequest.StatusCode.UNKNOWN)
                    .errorMsg(e.getMessage())
                    .build();
        }
    }

    public dev.jianmu.application.dsl.webhook.Webhook getWebhookParam(String id) {
        var webRequest = this.webRequestRepositoryImpl.findById(id)
                .orElseThrow(() -> new DataNotFoundException("未找到Webhook请求"));
        var workflow = this.workflowRepository.findByRefAndVersion(webRequest.getWorkflowRef(), webRequest.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义"));
        dev.jianmu.application.dsl.webhook.Webhook trigger = WebhookDslParser.parse(workflow.getDslText()).getTrigger();
        if (trigger.getParam() == null) {
            return trigger;
        }
        if (ObjectUtils.isEmpty(webRequest.getPayload())) {
            webRequest.setPayload(this.storageService.readWebhook(webRequest.getId()));
        }
        trigger.getParam().forEach(webhookParameter -> {
            var value = this.extractParameter(webRequest.getPayload(), webhookParameter.getExp(), webhookParameter.getType());
            var defaultParameter = Parameter.Type.getTypeByName(webhookParameter.getType()).newParameter(webhookParameter.getDefaultValue());
            webhookParameter.setDefaultValue(defaultParameter.getValue());
            webhookParameter.setValue(value == null ? defaultParameter.getValue() : value);
        });
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
        webRequest.setErrorMsg("待执行流程数已超过最大值");
        this.webRequestRepositoryImpl.update(webRequest);
    }

    public Optional<WebRequest> findWebRequestById(String webRequestId) {
        return this.webRequestRepositoryImpl.findById(webRequestId);
    }

    public Optional<Trigger> findTrigger(String projectId) {
        return this.triggerRepository.findByProjectId(projectId);
    }

    public Optional<WebRequest> findLatestWebRequest(String projectId) {
        return this.webRequestRepositoryImpl.findLatestByProjectId(projectId);
    }
}
