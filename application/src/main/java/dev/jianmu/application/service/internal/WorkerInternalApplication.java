package dev.jianmu.application.service.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.query.NodeDef;
import dev.jianmu.application.query.NodeDefApi;
import dev.jianmu.el.ElContext;
import dev.jianmu.event.Publisher;
import dev.jianmu.event.impl.WorkerDeferredResultClearEvent;
import dev.jianmu.infrastructure.GlobalProperties;
import dev.jianmu.infrastructure.storage.MonitoringFileService;
import dev.jianmu.infrastructure.worker.*;
import dev.jianmu.infrastructure.worker.event.TaskFailedEvent;
import dev.jianmu.infrastructure.worker.event.TaskFinishedEvent;
import dev.jianmu.infrastructure.worker.event.TaskRunningEvent;
import dev.jianmu.infrastructure.worker.unit.*;
import dev.jianmu.secret.aggregate.CredentialManager;
import dev.jianmu.secret.aggregate.KVPair;
import dev.jianmu.task.aggregate.InstanceParameter;
import dev.jianmu.task.aggregate.InstanceStatus;
import dev.jianmu.task.aggregate.TaskInstance;
import dev.jianmu.task.event.TaskInstanceCreatedEvent;
import dev.jianmu.task.repository.InstanceParameterRepository;
import dev.jianmu.task.repository.TaskInstanceRepository;
import dev.jianmu.task.repository.VolumeRepository;
import dev.jianmu.trigger.event.TriggerEvent;
import dev.jianmu.trigger.repository.TriggerEventRepository;
import dev.jianmu.worker.aggregate.Worker;
import dev.jianmu.worker.repository.WorkerRepository;
import dev.jianmu.workflow.aggregate.definition.Node;
import dev.jianmu.workflow.aggregate.definition.TaskParameter;
import dev.jianmu.workflow.aggregate.parameter.Parameter;
import dev.jianmu.workflow.aggregate.parameter.SecretParameter;
import dev.jianmu.workflow.aggregate.process.AsyncTaskInstance;
import dev.jianmu.workflow.aggregate.process.WorkflowInstance;
import dev.jianmu.workflow.el.EvaluationResult;
import dev.jianmu.workflow.el.Expression;
import dev.jianmu.workflow.el.ExpressionLanguage;
import dev.jianmu.workflow.repository.AsyncTaskInstanceRepository;
import dev.jianmu.workflow.repository.ParameterRepository;
import dev.jianmu.workflow.repository.WorkflowInstanceRepository;
import dev.jianmu.workflow.repository.WorkflowRepository;
import dev.jianmu.workflow.service.ParameterDomainService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Daihw
 * @class WorkerInternalApplication
 * @description WorkerInternalApplication
 * @create 2022/5/27 10:46 上午
 */
@Slf4j
@Service
public class WorkerInternalApplication {
    private static final Logger logger = LoggerFactory.getLogger(WorkerInternalApplication.class);
    private final String optionScript = "set -e";
    private final String traceScript = "\nset -x";
    private final String script = "\n%s";

    private final ParameterRepository parameterRepository;
    private final ParameterDomainService parameterDomainService;
    private final CredentialManager credentialManager;
    private final NodeDefApi nodeDefApi;
    private final WorkerRepository workerRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final InstanceParameterRepository instanceParameterRepository;
    private final TriggerEventRepository triggerEventRepository;
    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final TaskInstanceRepository taskInstanceRepository;
    private final ObjectMapper objectMapper;
    private final MonitoringFileService monitoringFileService;
    private final GlobalProperties globalProperties;
    private final WorkflowRepository workflowRepository;
    private final AsyncTaskInstanceRepository asyncTaskInstanceRepository;
    private final ExpressionLanguage expressionLanguage;
    private final Publisher publisher;
    private final VolumeRepository volumeRepository;

    public WorkerInternalApplication(
            ParameterRepository parameterRepository,
            ParameterDomainService parameterDomainService,
            CredentialManager credentialManager,
            NodeDefApi nodeDefApi,
            WorkerRepository workerRepository,
            ApplicationEventPublisher applicationEventPublisher,
            InstanceParameterRepository instanceParameterRepository,
            TriggerEventRepository triggerEventRepository,
            WorkflowInstanceRepository workflowInstanceRepository,
            TaskInstanceRepository taskInstanceRepository,
            ObjectMapper objectMapper,
            MonitoringFileService monitoringFileService,
            GlobalProperties globalProperties,
            WorkflowRepository workflowRepository,
            AsyncTaskInstanceRepository asyncTaskInstanceRepository,
            ExpressionLanguage expressionLanguage,
            Publisher publisher,
            VolumeRepository volumeRepository
    ) {
        this.parameterRepository = parameterRepository;
        this.parameterDomainService = parameterDomainService;
        this.credentialManager = credentialManager;
        this.nodeDefApi = nodeDefApi;
        this.workerRepository = workerRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.instanceParameterRepository = instanceParameterRepository;
        this.triggerEventRepository = triggerEventRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.taskInstanceRepository = taskInstanceRepository;
        this.objectMapper = objectMapper;
        this.monitoringFileService = monitoringFileService;
        this.globalProperties = globalProperties;
        this.workflowRepository = workflowRepository;
        this.asyncTaskInstanceRepository = asyncTaskInstanceRepository;
        this.expressionLanguage = expressionLanguage;
        this.publisher = publisher;
        this.volumeRepository = volumeRepository;
    }

    @Transactional
    public void join(String workerId, Worker.Type type, String name, String tag) {
        if (this.workerRepository.findById(workerId).isPresent()) {
            this.workerRepository.updateTag(Worker.Builder.aWorker()
                    .id(workerId)
                    .tags(tag)
                    .build());
            return;
        }
        this.workerRepository.add(Worker.Builder.aWorker()
                .id(workerId)
                .name(name)
                .type(type)
                .tags(tag)
                .status(Worker.Status.ONLINE)
                .build());
    }

    @Transactional
    public void dispatchTask(TaskInstanceCreatedEvent event) {
        var taskInstance = this.taskInstanceRepository.findById(event.getTaskInstanceId())
                .orElseThrow(() -> new RuntimeException("未找到任务实例：" + event.getTaskInstanceId()));
        if (taskInstance.getStatus() != InstanceStatus.INIT) {
            return;
        }
        try {
            if (!taskInstance.isVolume()) {
                var nodeDef = this.nodeDefApi.findByType(taskInstance.getDefKey());
                if (!nodeDef.getWorkerType().equals("DOCKER")) {
                    throw new RuntimeException("无法执行此类节点任务: " + nodeDef.getType());
                }
            }
            List<Worker> workers;
            if (taskInstance.isVolume()) {
                workers = this.workerRepository.findByTypeInAndCreatedTimeLessThan(
                        List.of(Worker.Type.DOCKER, Worker.Type.KUBERNETES),
                        LocalDateTime.now()
                );
            } else {
                var workflowInstance = this.workflowInstanceRepository.findByTriggerId(event.getTriggerId())
                        .orElseThrow(() -> new DataNotFoundException("未找到流程实例，triggerId：" + event.getTriggerId()));
                List<String> workerTags = this.getWorkerTag(workflowInstance);
                logger.info("triggerId:{} instanceId:{} tags: {}", workflowInstance.getTriggerId(), workflowInstance.getId(), workerTags);
                workers = workerTags.isEmpty() ?
                        this.workerRepository.findByTypeInAndCreatedTimeLessThan(
                                List.of(Worker.Type.DOCKER, Worker.Type.KUBERNETES),
                                workflowInstance.getStartTime()) :
                        this.workerRepository.findByTypeInAndTagAndCreatedTimeLessThan(
                                List.of(Worker.Type.DOCKER, Worker.Type.KUBERNETES),
                                workerTags, workflowInstance.getStartTime());
            }
            String volumeWorkerId = this.findCacheWorkerId(taskInstance);
            // 分发worker
            if (workers.isEmpty()) {
                throw new RuntimeException("worker数量为0，节点任务类型：" + Worker.Type.DOCKER);
            }
            var worker = DispatchWorker.getWorker(taskInstance.getTriggerId(), workers, volumeWorkerId, taskInstance.getAsyncTaskRef());
            taskInstance.setWorkerId(worker.getId());
            taskInstance.waiting();
            this.taskInstanceRepository.updateWorkerId(taskInstance);
            // 返回DeferredResult
            this.publisher.publish(WorkerDeferredResultClearEvent.builder()
                    .workerId(worker.getId())
                    .build());
        } catch (RuntimeException e) {
            logger.error("任务分发失败，", e);
            taskInstance.dispatchFailed();
            this.taskInstanceRepository.updateStatus(taskInstance);
        }
    }

    private String findCacheWorkerId(TaskInstance taskInstance) {
        var volumes = this.volumeRepository.findByWorkflowRef(taskInstance.getWorkflowRef()).stream()
                .filter(dev.jianmu.task.aggregate.Volume::isAvailable)
                .collect(Collectors.toList());
        return volumes.isEmpty() ? null : volumes.get(0).getWorkerId();
    }

    private List<String> getWorkerTag(WorkflowInstance workflowInstance) {
        var workflow = this.workflowRepository.findByRefAndVersion(workflowInstance.getWorkflowRef(), workflowInstance.getWorkflowVersion())
                .orElseThrow(() -> new RuntimeException(String.format("无法找到对应的流程定义: %s, %s", workflowInstance.getWorkflowRef(), workflowInstance.getWorkflowVersion())));


        // 查询参数源
        var eventParameters = this.triggerEventRepository.findById(workflowInstance.getTriggerId())
                .map(TriggerEvent::getParameters)
                .orElseGet(List::of);
        // 创建表达式上下文
        var context = new ElContext();
        // 全局参数加入上下文
        workflow.getGlobalParameters()
                .forEach(globalParameter -> context.add(
                        "global",
                        globalParameter.getName(),
                        Parameter.Type.getTypeByName(globalParameter.getType()).newParameter(globalParameter.getValue()))
                );
        // 事件参数加入上下文
        var eventParams = eventParameters.stream()
                .map(eventParameter -> Map.entry(eventParameter.getName(), eventParameter.getParameterId()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        var eventParamValues = this.parameterRepository.findByIds(new HashSet<>(eventParams.values()));
        var eventMap = this.parameterDomainService.matchParameters(eventParams, eventParamValues);
        // 事件参数scope为event
        eventMap.forEach((key, val) -> context.add("trigger", key, val));

        return workflow.getTags().stream().filter(StringUtils::hasText).map(tag -> {
            Expression el = this.expressionLanguage.parseExpression("`" + tag + "`");
            EvaluationResult result = this.expressionLanguage.evaluateExpression(el, context);
            if (result.isFailure()) {
                throw new RuntimeException("解析执行器标签的el表达式解析失败: " + result.getFailureMessage());
            } else if (!Parameter.Type.STRING.equals(result.getValue().getType())) {
                throw new RuntimeException("解析执行器标签的el表达式解析失败: 解析结果类型不正确");
            }
            return result.getValue().getStringValue();
        }).collect(Collectors.toList());
    }

    public Optional<TaskInstance> pullTasks(String workerId) {
        return this.taskInstanceRepository.findByWorkerIdAndTriggerIdLimit(workerId, null);
    }

    public Optional<TaskInstance> pullKubeTasks(String workerId, String triggerId) {
        return this.taskInstanceRepository.findByWorkerIdAndTriggerIdLimit(workerId, triggerId);
    }

    public ContainerSpec getContainerSpec(TaskInstance taskInstance) {
        // 查找节点定义
        var nodeDef = this.nodeDefApi.findByType(taskInstance.getDefKey());
        if (!nodeDef.getWorkerType().equals("DOCKER")) {
            throw new RuntimeException("无法执行此类节点任务: " + nodeDef.getType());
        }
        var isShellNode = nodeDef.getImage() != null;
        // 环境变量
        var worker = this.workerRepository.findById(taskInstance.getWorkerId())
                .orElseThrow(() -> new RuntimeException("未找到worker：" + taskInstance.getWorkerId()));
        var instanceParameters = this.instanceParameterRepository
                .findByInstanceIdAndType(taskInstance.getId(), InstanceParameter.Type.INPUT);
        // 查询参数值
        var parameters = this.parameterRepository.findByIds(instanceParameters.stream()
                .map(InstanceParameter::getParameterId)
                .collect(Collectors.toSet()));
        var parameterMap = this.getParameterMap(instanceParameters, parameters);
        var secretSet = this.getSecretParameterSet(isShellNode, instanceParameters, parameters);
        if (!isShellNode) {
            parameterMap = parameterMap.entrySet().stream()
                    .filter(entry -> entry.getKey() != null)
                    .map(entry -> Map.entry("JIANMU_" + entry.getKey(), entry.getValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        parameterMap.putAll(this.getEnvVariable(worker, taskInstance.getTriggerId(), taskInstance.getBusinessId(), taskInstance.getDefKey()));
        this.addFeatureParam(parameterMap);
        parameterMap = parameterMap.entrySet().stream()
                .filter(entry -> entry.getKey() != null)
                .map(entry -> Map.entry(entry.getKey().toUpperCase(), entry.getValue() == null ? "" : entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        // 查询node
        var workflow = this.workflowRepository.findByRefAndVersion(taskInstance.getWorkflowRef(), taskInstance.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程"));
        var node = workflow.findNode(taskInstance.getAsyncTaskRef());
        var volumeMounts = node.getTaskCaches() == null ? new ArrayList<VolumeMount>() : node.getTaskCaches().stream()
                .map(taskCache -> VolumeMount.builder()
                        .source(taskCache.getSource())
                        .target(taskCache.getTarget())
                        .build()
                )
                .collect(Collectors.toList());
        volumeMounts.add(VolumeMount.builder()
                .source(taskInstance.getTriggerId())
                .target("/" + taskInstance.getTriggerId())
                .build());

        // 创建ContainerSpec
        ContainerSpec newSpec;
        if (isShellNode) {
            var script = this.createScript(nodeDef.getScript());
            parameterMap.put("JIANMU_SCRIPT", script);
            String[] entrypoint = {"/bin/sh", "-c"};
            String[] args = {"echo \"$JIANMU_SCRIPT\" | /bin/sh"};
            newSpec = ContainerSpec.builder()
                    .image(nodeDef.getImage())
                    .working_dir("")
                    .environment(parameterMap)
                    .secrets(secretSet)
                    .entrypoint(entrypoint)
                    .args(args)
                    .volume_mounts(volumeMounts)
                    .extra_hosts(this.globalProperties.getWorker().getContainer().getExtraHosts())
                    .build();
        } else {
            dev.jianmu.embedded.worker.aggregate.spec.ContainerSpec spec;
            try {
                spec = objectMapper.readValue(nodeDef.getSpec(), dev.jianmu.embedded.worker.aggregate.spec.ContainerSpec.class);
            } catch (JsonProcessingException e) {
                log.error("拉取任务失败：", e);
                throw new RuntimeException("拉取任务失败");
            }
            newSpec = ContainerSpec.builder()
                    .image(spec.getImage())
                    .working_dir("")
                    .user(spec.getUser())
                    .host(spec.getHostName())
                    .environment(parameterMap)
                    .secrets(secretSet)
                    .entrypoint(spec.getEntrypoint())
                    .args(spec.getCmd())
                    .volume_mounts(volumeMounts)
                    .extra_hosts(this.globalProperties.getWorker().getContainer().getExtraHosts())
                    .build();
        }
        // 添加RegistryAddress
        newSpec.setRegistryAddress(globalProperties.getWorker().getRegistry().getAddress());
        return newSpec;
    }

    private String createScript(List<String> commands) {
        var sb = new StringBuilder();
        sb.append(optionScript);
        if (this.globalProperties.getTrace()) {
            sb.append(traceScript);
        }
        try (var formatter = new Formatter(sb, Locale.ROOT)) {
            commands.forEach(cmd -> formatter.format(script, cmd));
        }
        return sb.toString();
    }

    private Map<String, String> getParameterMap(List<InstanceParameter> instanceParameters, List<Parameter> parameters) {
        var parameterMap = instanceParameters.stream()
                .map(instanceParameter -> Map.entry(
                        instanceParameter.getRef(),
                        instanceParameter.getParameterId()
                        )
                )
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        // 替换实际参数值
        return this.parameterDomainService.createNoSecParameterMap(parameterMap, parameters);
    }

    private HashSet<WorkerSecret> getSecretParameterSet(boolean isShellNode, List<InstanceParameter> instanceParameters, List<Parameter> parameters) {
        var secretParameters = parameters.stream()
                .filter(parameter -> parameter instanceof SecretParameter)
                // 过滤非正常语法
                .filter(parameter -> parameter.getStringValue().split("\\.").length == 2)
                .collect(Collectors.toList());
        var secretSet = new HashSet<WorkerSecret>();
        instanceParameters.forEach(instanceParameter -> secretParameters.stream()
                .filter(parameter -> parameter.getId().equals(instanceParameter.getParameterId()))
                .findFirst()
                .ifPresent(parameter -> {
                    var kvPairOptional = this.findSecret(parameter);
                    kvPairOptional.ifPresent(kv -> {
                        var secretParameter = Parameter.Type.STRING.newParameter(kv.getValue());
                        secretSet.add(WorkerSecret.builder()
                                .env(isShellNode ? instanceParameter.getRef().toUpperCase() : "JIANMU_" + instanceParameter.getRef().toUpperCase())
                                .data(Base64.getEncoder().encodeToString(secretParameter.getStringValue().getBytes(StandardCharsets.UTF_8)))
                                .mask(true)
                                .build());
                    });
                }));
        return secretSet;
    }

    private Optional<KVPair> findSecret(Parameter<?> parameter) {
        // 处理密钥类型参数, 获取值后转换为String类型参数
        var strings = parameter.getStringValue().split("\\.");
        return this.credentialManager.findByNamespaceNameAndKey(strings[0], strings[1]);
    }

    /**
     * 设置一些通用参数到环境变量,方便在DSL中使用
     *
     * @param worker
     * @return
     */
    private HashMap<String, String> getEnvVariable(Worker worker, String triggerId, String businessId, String defKey) {

        HashMap<String, String> env = new HashMap<>();
        env.put("JM_RESULT_FILE", "/" + triggerId + "/" + businessId);
        env.put("JIANMU_SHARE_DIR", "/" + triggerId);
        env.put("JM_SHARE_DIR", "/" + triggerId);

        env.put("JM_WORKER_ID", worker.getId());
        env.put("JM_WORKER_TYPE", worker.getType().name());
        env.put("JM_BUSINESS_ID", businessId);
        env.put("JM_TRIGGER_ID", triggerId);
        env.put("JM_DEF_KEY", defKey);

        var triggerEvent = this.triggerEventRepository.findById(triggerId)
                .orElseThrow(() -> new DataNotFoundException("未找到该触发事件"));
        env.put("JM_PROJECT_ID", triggerEvent.getProjectId());
        env.put("JM_WEB_REQUEST_ID", triggerEvent.getWebRequestId());
        env.put("JM_TRIGGER_TIME", formatTime(triggerEvent.getOccurredTime()));
        env.put("JM_TRIGGER_TYPE", triggerEvent.getTriggerType());

        // workflow instance 相关参数
        WorkflowInstance workflowInstance = workflowInstanceRepository
                .findByTriggerId(triggerId)
                .orElseThrow(() -> new DataNotFoundException("未找到该workflow instance"));

        env.put("JM_INSTANCE_ID", workflowInstance.getId());
        env.put("JM_INSTANCE_TRIGGER_TYPE", workflowInstance.getTriggerType());
        env.put("JM_INSTANCE_WORKFLOW_REF", workflowInstance.getWorkflowRef());
        env.put("JM_INSTANCE_WORKFLOW_VERSION", workflowInstance.getWorkflowVersion());
        env.put("JM_INSTANCE_CREATE_TIME", formatTime(workflowInstance.getCreateTime()));
        env.put("JM_INSTANCE_START_TIME", formatTime(workflowInstance.getStartTime()));
        env.put("JM_INSTANCE_SUSPENDED_TIME", formatTime(workflowInstance.getSuspendedTime()));
        env.put("JM_INSTANCE_SERIAL_NO", workflowInstance.getSerialNo() + "");
        env.put("JM_INSTANCE_RUN_MODE", workflowInstance.getRunMode().name());
        env.put("JM_INSTANCE_STATUS", workflowInstance.getStatus().name());

        return env;
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? "" : time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 添加新的环境变量,以为JIANMU_开头的变量,都复制一份以为JM_开头的变量
     *
     * @param parameterMap
     * @return
     */
    private Map<String, String> addFeatureParam(Map<String, String> parameterMap) {
        var oldParameterMap = new HashMap<>(parameterMap);

        parameterMap.clear();
        oldParameterMap.forEach((key, value) -> {
            if (key != null && key.startsWith("JIANMU_")) {
                parameterMap.put(key.replaceFirst("JIANMU_", "JM_"), value);
            }
            parameterMap.put(key, value);
        });

        return parameterMap;
    }

    @Transactional
    public TaskInstance acceptTask(HttpServletResponse response, String workerId, String businessId, int version) {
        var taskInstance = this.taskInstanceRepository.findByBusinessIdAndVersion(businessId, version)
                .orElse(null);
        if (taskInstance == null) {
            response.setStatus(HttpStatus.SC_CONFLICT);
            return null;
        }
        if (taskInstance.getStatus() != InstanceStatus.WAITING) {
            response.setStatus(HttpStatus.SC_CONFLICT);
            return null;
        }
        taskInstance.acceptTask(version);
        if (!this.taskInstanceRepository.acceptTask(taskInstance)) {
            response.setStatus(HttpStatus.SC_CONFLICT);
        }
        return taskInstance;
    }

    @Transactional
    public void updateTaskInstance(String workerId, String businessId, String status, String resultFile, String errorMsg, Integer exitCode) {
        var taskInstance = this.taskInstanceRepository.findByBusinessIdAndMaxSerialNo(businessId)
                .orElseThrow(() -> new RuntimeException("未找到任务实例, businessId：" + businessId));
        switch (status) {
            case "RUNNING":
                this.applicationEventPublisher.publishEvent(TaskRunningEvent.builder()
                        .taskId(taskInstance.getId())
                        .workerId(workerId)
                        .build());
                break;
            case "FAILED":
                this.applicationEventPublisher.publishEvent(TaskFailedEvent.builder()
                        .workerId(workerId)
                        .taskId(taskInstance.getId())
                        .errorMsg(errorMsg)
                        .build());
                break;
            case "SUCCEED":
                this.applicationEventPublisher.publishEvent(TaskFinishedEvent.builder()
                        .workerId(workerId)
                        .taskId(taskInstance.getId())
                        .cmdStatusCode(exitCode)
                        .resultFile(resultFile)
                        .build());
                break;
        }
    }

    public void writeTaskLog(BufferedWriter logWriter, String workerId, String taskInstanceId, String content, Long number, Long timestamp) {
        if (content == null) {
            return;
        }
        try {
            logWriter.write(content);
            logWriter.flush();
        } catch (IOException e) {
            logger.error("任务日志写入失败：", e);
        }
        this.monitoringFileService.sendLog(taskInstanceId);
    }

    // 获取k8s Unit
    public Unit findUnit(TaskInstance taskInstance) {
        if (taskInstance.isCreationVolume()) {
            return this.findCreateUnit(taskInstance);
        } else if (taskInstance.isDeletionVolume()) {
            return Unit.builder()
                    .type(Unit.Type.DELETE)
                    .podSpec(PodSpec.builder()
                            .name(taskInstance.getTriggerId())
                            .build())
                    .pullSecret(this.findPullSecret())
                    .current(Runner.builder()
                            .id(taskInstance.getBusinessId())
                            .version(taskInstance.getVersion())
                            .build())
                    .build();
        } else {
            return Unit.builder()
                    .type(Unit.Type.RUN)
                    .podSpec(PodSpec.builder()
                            .name(taskInstance.getTriggerId())
                            .build())
                    .pullSecret(this.findPullSecret())
                    .current(this.findCurrentRunner(taskInstance))
                    .build();
        }
    }

    private WorkerSecret findPullSecret() {
        if (this.globalProperties.getWorker().getRegistry().getAddress() == null) {
            return null;
        }
        var auths = new HashMap<String, Map<String, String>>();
        var auth = new HashMap<String, String>();
        auth.put("username", this.globalProperties.getWorker().getRegistry().getUsername());
        auth.put("password", this.globalProperties.getWorker().getRegistry().getPassword());
        auths.put(this.globalProperties.getWorker().getRegistry().getAddress(), auth);
        String data = null;
        try {
            data = objectMapper.writeValueAsString(Map.entry("auths", auths));
        } catch (JsonProcessingException e) {
            log.error("pullSecret序列化失败，" + e);
        }
        return WorkerSecret.builder()
                .env("PULLSECRET")
                .data(data)
                .mask(true)
                .build();
    }

    private Unit findCreateUnit(TaskInstance taskInstance) {
        var worker = this.workerRepository.findById(taskInstance.getWorkerId())
                .orElseThrow(() -> new RuntimeException("未找到worker：" + taskInstance.getWorkerId()));
        var workflow = this.workflowRepository.findByRefAndVersion(taskInstance.getWorkflowRef(), taskInstance.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义"));
        var asyncTaskInstances = this.asyncTaskInstanceRepository.findByTriggerId(taskInstance.getTriggerId());
        var unitSecrets = new ArrayList<WorkerSecret>();
        var runners = new ArrayList<Runner>();
        workflow.findTasks().forEach(node -> {
            var nodeDef = this.nodeDefApi.findByType(node.getType());
            var isShellNode = nodeDef.getImage() != null;
            var runnerSecrets = new ArrayList<SecretVar>();
            var runnerEnvs = new HashMap<String, String>();
            node.getTaskParameters().forEach(taskParameter -> {
                if (taskParameter.getType() == Parameter.Type.SECRET) {
                    this.findUnitSecret(taskParameter, nodeDef).ifPresent(workerSecret -> {
                        unitSecrets.add(workerSecret);
                        runnerSecrets.add(SecretVar.builder()
                                .env(workerSecret.getEnv())
                                .name(workerSecret.getEnv())
                                .build());
                    });
                } else {
                    runnerEnvs.put((isShellNode ? "" : "JIANMU_") + taskParameter.getRef().toUpperCase(), taskParameter.getExpression());
                }
            });
            runners.add(this.findUnitRunner(asyncTaskInstances, nodeDef, node, runnerSecrets, runnerEnvs, worker));
        });
        // 添加keepalive
        var keepAliveRunner = Runner.builder()
                .id("keep-alive")
                .placeholder(this.globalProperties.getWorker().getK8s().getKeepalive())
                .entrypoint(new String[]{"tail", "-f", "/dev/null"})
                .volumes(runners.get(0).getVolumes())
                .build();
        runners.add(keepAliveRunner);
        var startRunner = Runner.builder()
                .id(taskInstance.getBusinessId())
                .version(taskInstance.getVersion())
                .volumes(List.of(
                        K8sVolumeMount.builder()
                                .name(taskInstance.getTriggerId())
                                .path("/" + taskInstance.getTriggerId())
                                .build()
                ))
                .build();
        return Unit.builder()
                .type(Unit.Type.CREATE)
                .podSpec(PodSpec.builder()
                        .name(taskInstance.getTriggerId())
                        .build())
                .volumes(List.of(
                        Volume.builder()
                                .volumeHostPath(VolumeHostPath.builder()
                                        .id("tempdir")
                                        .name("tempdir")
                                        .path("/tmp/" + taskInstance.getTriggerId())
                                        .type("dir")
                                        .build())
                                .build(),
                        Volume.builder()
                                .volumeHostPath(VolumeHostPath.builder()
                                        .id(taskInstance.getTriggerId())
                                        .name(taskInstance.getTriggerId())
                                        .path("/tmp/" + taskInstance.getTriggerId() + "/resultFile")
                                        .type("file")
                                        .build())
                                .build()
                ))
                .secrets(unitSecrets)
                .pullSecret(this.findPullSecret())
                .current(startRunner)
                .runners(runners)
                .build();
    }

    private Optional<WorkerSecret> findUnitSecret(TaskParameter taskParameter, NodeDef nodeDef) {
        var parameter = Parameter.Type.SECRET.newParameter(this.findSecretByExpression(taskParameter.getExpression()));
        if (parameter.getStringValue().split("\\.").length == 2) {
            var kvPairOptional = this.findSecret(parameter);
            return kvPairOptional.map(kv -> {
                var secretParameter = Parameter.Type.STRING.newParameter(kv.getValue());
                return Optional.of(WorkerSecret.builder()
                        .env(nodeDef.getImage() != null ? taskParameter.getRef().toUpperCase() : "JIANMU_" + taskParameter.getRef().toUpperCase())
                        .data(Base64.getEncoder().encodeToString(secretParameter.getStringValue().getBytes(StandardCharsets.UTF_8)))
                        .mask(true)
                        .build());
            }).orElse(Optional.empty());
        }
        return Optional.empty();
    }

    private String findSecretByExpression(String paramValue) {
        Pattern pattern = Pattern.compile("^\\(\\(([a-zA-Z0-9_-]+\\.*[a-zA-Z0-9_-]+)\\)\\)$");
        Matcher matcher = pattern.matcher(paramValue);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private Runner findUnitRunner(List<AsyncTaskInstance> asyncTaskInstances, NodeDef nodeDef, Node node, List<SecretVar> secretVars, Map<String, String> envs, Worker worker) {
        Runner runner;
        var asyncTaskInstance = asyncTaskInstances.stream()
                .filter(t -> t.getAsyncTaskRef().equals(node.getRef()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未找到对应的异步任务实例："));
        envs.put("JM_RESULT_FILE", "/" + asyncTaskInstance.getTriggerId() + "/" + asyncTaskInstance.getId());
        var map = this.getEnvVariable(worker, asyncTaskInstance.getTriggerId(), asyncTaskInstance.getId(), asyncTaskInstance.getAsyncTaskType());
        envs.putAll(map);
        if (nodeDef.getImage() != null) {
            envs.put("JIANMU_SCRIPT", "JIANMU_SCRIPT");
            String[] entrypoint = {"/bin/sh", "-c"};
            String[] command = {"echo \"$JIANMU_SCRIPT\" | /bin/sh"};
            runner = Runner.builder()
                    .id(asyncTaskInstance.getId())
                    .version(0)
                    .command(command)
                    .entrypoint(entrypoint)
                    .envs(envs)
                    .image(nodeDef.getImage())
                    .name(node.getRef())
                    .placeholder(this.globalProperties.getWorker().getK8s().getPlaceholder())
                    .secrets(secretVars)
                    .volumes(List.of(
                            K8sVolumeMount.builder()
                                    .name("tempdir")
                                    .path("/" + asyncTaskInstance.getTriggerId())
                                    .build()
                    ))
                    .build();
        } else {
            dev.jianmu.embedded.worker.aggregate.spec.ContainerSpec spec;
            try {
                spec = objectMapper.readValue(nodeDef.getSpec(), dev.jianmu.embedded.worker.aggregate.spec.ContainerSpec.class);
            } catch (JsonProcessingException e) {
                log.error("拉取任务失败：", e);
                throw new RuntimeException("拉取任务失败");
            }
            var builder = Runner.builder()
                    .id(asyncTaskInstance.getId())
                    .version(0)
                    .command(spec.getCmd())
                    .entrypoint(spec.getEntrypoint())
                    .envs(envs)
                    .image(spec.getImage())
                    .name(node.getRef())
                    .placeholder(this.globalProperties.getWorker().getK8s().getPlaceholder())
                    .secrets(secretVars)
                    .volumes(List.of(
                            K8sVolumeMount.builder()
                                    .name("tempdir")
                                    .path("/" + asyncTaskInstance.getTriggerId())
                                    .build(),
                            K8sVolumeMount.builder()
                                    .name(asyncTaskInstance.getTriggerId())
                                    .path(nodeDef.getResultFile())
                                    .build()
                    ));
            if (nodeDef.getResultFile() == null || nodeDef.getResultFile().isBlank()) {
                builder.volumes(List.of(
                        K8sVolumeMount.builder()
                                .name("tempdir")
                                .path("/" + asyncTaskInstance.getTriggerId())
                                .build()
                ));
            } else {
                builder.volumes(List.of(
                        K8sVolumeMount.builder()
                                .name("tempdir")
                                .path("/" + asyncTaskInstance.getTriggerId())
                                .build(),
                        K8sVolumeMount.builder()
                                .name(asyncTaskInstance.getTriggerId())
                                .path(nodeDef.getResultFile())
                                .build()
                ));
            }
            runner = builder.build();
        }
        runner.setRegistryAddress(globalProperties.getWorker().getRegistry().getAddress());
        return runner;

    }

    private Runner findCurrentRunner(TaskInstance taskInstance) {
        var containerSpec = this.getContainerSpec(taskInstance);
        var secrets = containerSpec.getSecrets().stream()
                .map(workerSecret -> SecretVar.builder()
                        .name(workerSecret.getEnv())
                        .env(workerSecret.getData())
                        .build())
                .collect(Collectors.toList());
        return Runner.builder()
                .id(taskInstance.getBusinessId())
                .version(taskInstance.getVersion())
                .command(containerSpec.getArgs())
                .entrypoint(containerSpec.getEntrypoint())
                .envs(containerSpec.getEnvironment())
                .image(containerSpec.getImage())
                .name(taskInstance.getAsyncTaskRef())
                .placeholder(this.globalProperties.getWorker().getK8s().getPlaceholder())
                .secrets(secrets)
                .resultFile("/" + taskInstance.getTriggerId() + "/resultFile")
                .build();
    }

    public Unit findRunningTaskByTriggerId(String workerId, String triggerId) {
        var workflowInstance = this.workflowInstanceRepository.findByTriggerId(triggerId)
                .orElseThrow(() -> new DataNotFoundException("未找到流程实例：" + triggerId));
        List<Runner> runners;
        if (workflowInstance.isRunning()) {
            runners = this.taskInstanceRepository.findByTriggerIdAndStatus(triggerId, InstanceStatus.RUNNING).stream()
                    .map(this::findCurrentRunner)
                    .collect(Collectors.toList());
        } else {
            runners = List.of();
        }
        return Unit.builder()
                .type(Unit.Type.RUN)
                .podSpec(PodSpec.builder()
                        .name(triggerId)
                        .build())
                .pullSecret(this.findPullSecret())
                .runners(runners)
                .build();
    }
}
