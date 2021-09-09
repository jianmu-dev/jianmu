package dev.jianmu.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.query.NodeDefApi;
import dev.jianmu.el.ElContext;
import dev.jianmu.eventbridge.repository.TargetEventRepository;
import dev.jianmu.hub.intergration.aggregate.NodeParameter;
import dev.jianmu.task.aggregate.Definition;
import dev.jianmu.task.aggregate.InstanceParameter;
import dev.jianmu.task.aggregate.TaskInstance;
import dev.jianmu.task.repository.DefinitionRepository;
import dev.jianmu.task.repository.InstanceParameterRepository;
import dev.jianmu.task.repository.TaskInstanceRepository;
import dev.jianmu.task.service.InstanceDomainService;
import dev.jianmu.workflow.aggregate.parameter.Parameter;
import dev.jianmu.workflow.el.ExpressionLanguage;
import dev.jianmu.workflow.event.TaskActivatingEvent;
import dev.jianmu.workflow.repository.ParameterRepository;
import dev.jianmu.workflow.repository.WorkflowRepository;
import dev.jianmu.workflow.service.ParameterDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @class: TaskInstanceApplication
 * @description: 任务实例门面类
 * @author: Ethan Liu
 * @create: 2021-03-25 20:33
 **/
@Service
public class TaskInstanceApplication {
    private static final Logger logger = LoggerFactory.getLogger(TaskInstanceApplication.class);

    private final TaskInstanceRepository taskInstanceRepository;
    private final WorkflowRepository workflowRepository;
    private final DefinitionRepository definitionRepository;
    private final InstanceDomainService instanceDomainService;
    private final ParameterRepository parameterRepository;
    private final ParameterDomainService parameterDomainService;
    private final TargetEventRepository targetEventRepository;
    private final InstanceParameterRepository instanceParameterRepository;
    private final NodeDefApi nodeDefApi;
    private final ExpressionLanguage expressionLanguage;

    @Inject
    public TaskInstanceApplication(
            TaskInstanceRepository taskInstanceRepository,
            WorkflowRepository workflowRepository,
            DefinitionRepository definitionRepository,
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
        this.definitionRepository = definitionRepository;
        this.instanceDomainService = instanceDomainService;
        this.parameterRepository = parameterRepository;
        this.parameterDomainService = parameterDomainService;
        this.targetEventRepository = targetEventRepository;
        this.instanceParameterRepository = instanceParameterRepository;
        this.nodeDefApi = nodeDefApi;
        this.expressionLanguage = expressionLanguage;
    }

    public List<InstanceParameter> findParameters(String instanceId) {
        return this.instanceParameterRepository.findByInstanceId(instanceId);
    }

    public List<TaskInstance> findByBusinessId(String businessId) {
        return this.taskInstanceRepository.findByBusinessId(businessId);
    }

    public Optional<TaskInstance> findById(String instanceId) {
        return this.taskInstanceRepository.findById(instanceId);
    }

    @Transactional
    public void create(TaskActivatingEvent event) {
        var workflow = this.workflowRepository.findByRefAndVersion(event.getWorkflowRef(), event.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义: " + event.getWorkflowRef()));
        var asyncTask = workflow.findNode(event.getNodeRef());
        var nodeDef = this.nodeDefApi.findByType(asyncTask.getType());
        // 创建任务实例
        List<TaskInstance> taskInstances = this.taskInstanceRepository.findByAsyncTaskRefAndBusinessId(event.getNodeRef(), event.getWorkflowInstanceId());
        // 运行前检查规则
        this.instanceDomainService.runningCheck(taskInstances);
        var taskInstance = TaskInstance.Builder.anInstance()
                .serialNo(taskInstances.size() + 1)
                .defKey(asyncTask.getType())
                .asyncTaskRef(asyncTask.getRef())
                .workflowRef(workflow.getRef())
                .workflowVersion(workflow.getVersion())
                .businessId(event.getWorkflowInstanceId())
                .triggerId(event.getTriggerId())
                .build();
        // 查询参数源
        var eventParameters = this.targetEventRepository.findById(event.getTriggerId())
                .orElseThrow(() -> new DataNotFoundException("未找到事件" + event.getTriggerId()))
                .getEventParameters();
        var instanceParameters = this.instanceParameterRepository
                .findOutputParamByBusinessIdAndTriggerId(event.getWorkflowInstanceId(), event.getTriggerId());
        // 创建表达式上下文
        var context = new ElContext();
        // 全局参数加入上下文
        workflow.getGlobalParameters()
                .forEach(globalParameter -> context.add("global", globalParameter.getName(), Parameter.Type.STRING.newParameter(globalParameter.getValue())));
        // 事件参数加入上下文
        var eventParams = eventParameters.stream()
                .map(eventParameter -> Map.entry(eventParameter.getName(), eventParameter.getParameterId()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        var eventParamValues = this.parameterRepository.findByIds(new HashSet<>(eventParams.values()));
        var eventMap = this.parameterDomainService.matchParameters(eventParams, eventParamValues);
        // 事件参数scope为event
        eventMap.forEach((key, val) -> context.add("event", key, val));
        // 任务输出参数加入上下文
        var outParams = instanceParameters.stream()
                // 输出参数scope为asyncTaskRef
                .map(instanceParameter -> Map.entry(instanceParameter.getAsyncTaskRef() + "." + instanceParameter.getRef(), instanceParameter.getParameterId()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        var outParamValues = this.parameterRepository.findByIds(new HashSet<>(outParams.values()));
        var outMap = this.parameterDomainService.matchParameters(outParams, outParamValues);
        outMap.forEach(context::add);

        workflow.setExpressionLanguage(this.expressionLanguage);
        workflow.setContext(context);
        var params = workflow.calculateTaskParams(asyncTask.getRef());

        // 创建任务实例输入参数
        var instanceInputParameters = this.createInstanceParameters(params, taskInstance, nodeDef.getInputParameters());

        // 保存参数
        this.parameterRepository.addAll(new ArrayList<>(params.values()));
        // 保存任务实例输入参数
        this.instanceParameterRepository.addAll(instanceInputParameters);
        // 保存任务实例
        this.taskInstanceRepository.add(taskInstance);
    }

    private Set<InstanceParameter> createInstanceParameters(Map<String, Parameter<?>> parameterMap, TaskInstance taskInstance, Set<NodeParameter> nodeParameters) {
        var instanceParameters = parameterMap.entrySet().stream().map(entry ->
                InstanceParameter.Builder.anInstanceParameter()
                        .instanceId(taskInstance.getId())
                        .triggerId(taskInstance.getTriggerId())
                        .defKey(taskInstance.getDefKey())
                        .asyncTaskRef(taskInstance.getAsyncTaskRef())
                        .businessId(taskInstance.getBusinessId())
                        .ref(entry.getKey())
                        .parameterId(entry.getValue().getId())
                        .type(InstanceParameter.Type.INPUT)
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
        String[] strings = taskInstance.getDefKey().split(":");
        var definition = this.definitionRepository.findByRefAndVersion(strings[0], strings[1])
                .orElseThrow(() -> new DataNotFoundException("未找到该任务定义"));
        if (definition.getResultFile() != null) {
            // 解析Json为Map
            var parameterMap = this.parseJson(resultFile);
            // 创建任务实例输出参数与参数存储参数
            var outputParameters = this.handleOutputParameter(parameterMap, definition, taskInstance);
            // 保存任务实例输出参数
            this.instanceParameterRepository.addAll(outputParameters.keySet());
            // 保存参数
            this.parameterRepository.addAll(new ArrayList<>(outputParameters.values()));
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
            throw new RuntimeException("任务结果文件为空");
        }
        var objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(resultFile, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            logger.error("Json转换", e);
            throw new RuntimeException("任务结果文件格式错误");
        }
    }

    private Map<InstanceParameter, Parameter<?>> handleOutputParameter(Map<String, Object> parameterMap, Definition definition, TaskInstance taskInstance) {
        // 查找需赋值的输出参数
        var outputParameters = definition.matchedOutputParameters(parameterMap);
        return outputParameters.stream()
                .map(taskParameter -> {
                    var value = parameterMap.get(taskParameter.getRef());
                    // 创建参数
                    var parameter = Parameter.Type.valueOf(taskParameter.getType()).newParameter(value);
                    // 创建任务实例输出参数
                    var instanceParameter = InstanceParameter.Builder.anInstanceParameter()
                            .instanceId(taskInstance.getId())
                            .serialNo(taskInstance.getSerialNo())
                            .asyncTaskRef(taskInstance.getAsyncTaskRef())
                            .defKey(taskInstance.getDefKey())
                            .businessId(taskInstance.getBusinessId())
                            .triggerId(taskInstance.getTriggerId())
                            .ref(taskParameter.getRef())
                            .type(InstanceParameter.Type.OUTPUT)
                            .parameterId(parameter.getId())
                            .build();
                    return Map.entry(instanceParameter, parameter);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
