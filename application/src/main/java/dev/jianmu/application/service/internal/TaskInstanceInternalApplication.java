package dev.jianmu.application.service.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.query.NodeDef;
import dev.jianmu.application.query.NodeDefApi;
import dev.jianmu.el.ElContext;
import dev.jianmu.eventbridge.aggregate.event.TargetEvent;
import dev.jianmu.eventbridge.repository.TargetEventRepository;
import dev.jianmu.hub.intergration.aggregate.NodeParameter;
import dev.jianmu.task.aggregate.InstanceParameter;
import dev.jianmu.task.aggregate.NodeInfo;
import dev.jianmu.task.aggregate.TaskInstance;
import dev.jianmu.task.repository.InstanceParameterRepository;
import dev.jianmu.task.repository.TaskInstanceRepository;
import dev.jianmu.task.service.InstanceDomainService;
import dev.jianmu.workflow.aggregate.parameter.Parameter;
import dev.jianmu.workflow.el.ExpressionLanguage;
import dev.jianmu.workflow.event.TaskActivatingEvent;
import dev.jianmu.workflow.repository.ParameterRepository;
import dev.jianmu.workflow.repository.WorkflowRepository;
import dev.jianmu.workflow.service.ParameterDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @class: TaskInstanceInternalApplication
 * @description: TaskInstanceInternalApplication
 * @author: Ethan Liu
 * @create: 2021-10-21 15:25
 **/
@Service
@Slf4j
public class TaskInstanceInternalApplication {
    private final TaskInstanceRepository taskInstanceRepository;
    private final WorkflowRepository workflowRepository;
    private final InstanceDomainService instanceDomainService;
    private final ParameterRepository parameterRepository;
    private final ParameterDomainService parameterDomainService;
    private final TargetEventRepository targetEventRepository;
    private final InstanceParameterRepository instanceParameterRepository;
    private final NodeDefApi nodeDefApi;
    private final ExpressionLanguage expressionLanguage;

    public TaskInstanceInternalApplication(
            TaskInstanceRepository taskInstanceRepository,
            WorkflowRepository workflowRepository,
            InstanceDomainService instanceDomainService,
            ParameterRepository parameterRepository,
            ParameterDomainService parameterDomainService,
            TargetEventRepository targetEventRepository,
            InstanceParameterRepository instanceParameterRepository,
            NodeDefApi nodeDefApi,
            ExpressionLanguage expressionLanguage
    ) {
        this.taskInstanceRepository = taskInstanceRepository;
        this.workflowRepository = workflowRepository;
        this.instanceDomainService = instanceDomainService;
        this.parameterRepository = parameterRepository;
        this.parameterDomainService = parameterDomainService;
        this.targetEventRepository = targetEventRepository;
        this.instanceParameterRepository = instanceParameterRepository;
        this.nodeDefApi = nodeDefApi;
        this.expressionLanguage = expressionLanguage;
    }

    public List<TaskInstance> findRunningTask() {
        return this.taskInstanceRepository.findRunningTask();
    }

    @Transactional
    public void create(TaskActivatingEvent event) {
        var workflow = this.workflowRepository.findByRefAndVersion(event.getWorkflowRef(), event.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义: " + event.getWorkflowRef()));
        var asyncTask = workflow.findNode(event.getNodeRef());
        var nodeDef = this.nodeDefApi.getByType(asyncTask.getType());
        // 创建任务实例
        List<TaskInstance> taskInstances = this.taskInstanceRepository.findByAsyncTaskRefAndBusinessId(event.getNodeRef(), event.getWorkflowInstanceId());
        // 运行前检查规则
        this.instanceDomainService.runningCheck(taskInstances);
        var nodeInfo = NodeInfo.Builder.aNodeDef()
                .name(nodeDef.getName())
                .icon(nodeDef.getIcon())
                .description(nodeDef.getDescription())
                .creatorName(nodeDef.getCreatorName())
                .creatorRef(nodeDef.getCreatorRef())
                .ownerName(nodeDef.getOwnerName())
                .ownerRef(nodeDef.getOwnerRef())
                .ownerType(nodeDef.getOwnerType())
                .workerType(nodeDef.getWorkerType())
                .type(nodeDef.getType())
                .documentLink(nodeDef.getDocumentLink())
                .sourceLink(nodeDef.getSourceLink())
                .build();
        var taskInstance = TaskInstance.Builder.anInstance()
                .serialNo(taskInstances.size() + 1)
                .defKey(asyncTask.getType())
                .nodeInfo(nodeInfo)
                .asyncTaskRef(asyncTask.getRef())
                .workflowRef(workflow.getRef())
                .workflowVersion(workflow.getVersion())
                .businessId(event.getWorkflowInstanceId())
                .triggerId(event.getTriggerId())
                .build();
        // 查询参数源
        var eventParameters = this.targetEventRepository.findById(event.getTriggerId())
                .map(TargetEvent::getEventParameters)
                .orElseGet(Set::of);
        var instanceParameters = this.instanceParameterRepository
                .findOutputParamByBusinessIdAndTriggerId(event.getWorkflowInstanceId(), event.getTriggerId());
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
        eventMap.forEach((key, val) -> context.add("event", key, val));
        // 任务输出参数加入上下文
        Map<String, String> outParams = new HashMap<>();
        instanceParameters.forEach(instanceParameter -> {
            // 输出参数scope为workflowType.asyncTaskRef
            outParams.put(instanceParameter.getWorkflowType() + "." + instanceParameter.getAsyncTaskRef() + "." + instanceParameter.getRef(), instanceParameter.getParameterId());
            // 输出参数scope为asyncTaskRef
            outParams.put(instanceParameter.getAsyncTaskRef() + "." + instanceParameter.getRef(), instanceParameter.getParameterId());
        });
        var outParamValues = this.parameterRepository.findByIds(new HashSet<>(outParams.values()));
        var outMap = this.parameterDomainService.matchParameters(outParams, outParamValues);
        outMap.forEach(context::add);

        workflow.setExpressionLanguage(this.expressionLanguage);
        workflow.setContext(context);
        var params = workflow.calculateTaskParams(asyncTask.getRef());

        // 创建任务实例输入参数
        var instanceInputParameters = this.createInstanceParameters(params, taskInstance, workflow.getType().name(), nodeDef.getInputParameters());

        // 保存参数
        this.parameterRepository.addAll(new ArrayList<>(params.values()));
        // 保存任务实例输入参数
        this.instanceParameterRepository.addAll(instanceInputParameters);
        // 保存任务实例
        this.taskInstanceRepository.add(taskInstance);
    }

    private Set<InstanceParameter> createInstanceParameters(
            Map<String, Parameter<?>> parameterMap,
            TaskInstance taskInstance,
            String workflowType,
            Set<NodeParameter> nodeParameters
    ) {
        var instanceParameters = parameterMap.entrySet().stream()
                .filter(entry ->
                        nodeParameters.stream()
                                .anyMatch(nodeParameter -> nodeParameter.getRef().equals(entry.getKey()))
                )
                .map(entry ->
                        InstanceParameter.Builder.anInstanceParameter()
                                .instanceId(taskInstance.getId())
                                .triggerId(taskInstance.getTriggerId())
                                .defKey(taskInstance.getDefKey())
                                .asyncTaskRef(taskInstance.getAsyncTaskRef())
                                .businessId(taskInstance.getBusinessId())
                                .ref(entry.getKey())
                                .parameterId(entry.getValue().getId())
                                .required(nodeParameters.stream()
                                        .filter(nodeParameter -> nodeParameter.getRef().equals(entry.getKey()))
                                        .findFirst()
                                        .map(NodeParameter::getRequired)
                                        .orElse(false)
                                )
                                .type(InstanceParameter.Type.INPUT)
                                .workflowType(workflowType)
                                .build()
                ).collect(Collectors.toSet());
        // 合并节点定义的默认参数与DSL中指定的参数
        nodeParameters.forEach(nodeParameter -> {
            // 如果不存在则使用默认参数
            if (!parameterMap.containsKey(nodeParameter.getRef())) {
                var p = InstanceParameter.Builder.anInstanceParameter()
                        .instanceId(taskInstance.getId())
                        .triggerId(taskInstance.getTriggerId())
                        .defKey(taskInstance.getDefKey())
                        .asyncTaskRef(taskInstance.getAsyncTaskRef())
                        .businessId(taskInstance.getBusinessId())
                        .ref(nodeParameter.getRef())
                        .parameterId(nodeParameter.getParameterId())
                        .type(InstanceParameter.Type.INPUT)
                        .required(nodeParameter.getRequired())
                        .workflowType(workflowType)
                        .build();
                instanceParameters.add(p);
            }
        });
        return instanceParameters;
    }

    @Transactional
    public void executeSucceeded(String taskInstanceId, String resultFile) {
        TaskInstance taskInstance = this.taskInstanceRepository.findById(taskInstanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该任务实例"));
        var workflow = this.workflowRepository.findByRefAndVersion(taskInstance.getWorkflowRef(), taskInstance.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义: " + taskInstance.getWorkflowRef()));
        var nodeVersion = this.nodeDefApi.findByType(taskInstance.getDefKey());
        if (nodeVersion.getResultFile() != null) {
            try {
                // 解析Json为Map
                var parameterMap = this.parseJson(resultFile);
                // 创建任务实例输出参数与参数存储参数
                var outputParameters = this.handleOutputParameter(
                        parameterMap, nodeVersion, workflow.getType().name(), taskInstance
                );
                // 保存任务实例输出参数
                this.instanceParameterRepository.addAll(outputParameters.keySet());
                // 保存参数
                this.parameterRepository.addAll(new ArrayList<>(outputParameters.values()));
            } catch (RuntimeException e) {
                log.warn("e: ", e);
                taskInstance.executeFailed();
                this.taskInstanceRepository.updateStatus(taskInstance);
                return;
            }
        }
        taskInstance.executeSucceeded(resultFile);
        this.taskInstanceRepository.saveSucceeded(taskInstance);
    }

    @Transactional
    public void executeFailed(String taskInstanceId) {
        TaskInstance taskInstance = this.taskInstanceRepository.findById(taskInstanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该任务实例"));
        taskInstance.executeFailed();
        this.taskInstanceRepository.updateStatus(taskInstance);
    }

    @Transactional
    public void running(String taskInstanceId) {
        TaskInstance taskInstance = this.taskInstanceRepository.findById(taskInstanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该任务实例"));
        taskInstance.running();
        this.taskInstanceRepository.updateStatus(taskInstance);
    }

    private Map<String, Object> parseJson(String resultFile) {
        if (resultFile == null || resultFile.isBlank()) {
            throw new IllegalArgumentException("任务结果文件为空");
        }
        var objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(resultFile, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            log.error("Json转换", e);
            throw new IllegalArgumentException("任务结果文件格式错误");
        }
    }

    private Map<InstanceParameter, Parameter<?>> handleOutputParameter(
            Map<String, Object> parameterMap,
            NodeDef nodeDef,
            String workflowType,
            TaskInstance taskInstance
    ) {
        // 查找需赋值的输出参数
        var outputParameters = nodeDef.matchedOutputParameters(parameterMap);
        return outputParameters.stream()
                .map(nodeParameter -> {
                    var value = parameterMap.get(nodeParameter.getRef());
                    // 创建参数
                    var parameter = Parameter.Type.getTypeByName(nodeParameter.getType()).newParameter(value);
                    // 创建任务实例输出参数
                    var instanceParameter = InstanceParameter.Builder.anInstanceParameter()
                            .instanceId(taskInstance.getId())
                            .serialNo(taskInstance.getSerialNo())
                            .asyncTaskRef(taskInstance.getAsyncTaskRef())
                            .defKey(taskInstance.getDefKey())
                            .businessId(taskInstance.getBusinessId())
                            .triggerId(taskInstance.getTriggerId())
                            .ref(nodeParameter.getRef())
                            .type(InstanceParameter.Type.OUTPUT)
                            .required(nodeParameter.getRequired())
                            .workflowType(workflowType)
                            .parameterId(parameter.getId())
                            .build();
                    return Map.entry(instanceParameter, parameter);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
