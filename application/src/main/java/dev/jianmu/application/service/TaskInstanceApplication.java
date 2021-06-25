package dev.jianmu.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.parameter.aggregate.Parameter;
import dev.jianmu.parameter.repository.ParameterRepository;
import dev.jianmu.task.aggregate.Definition;
import dev.jianmu.task.aggregate.InstanceParameter;
import dev.jianmu.task.aggregate.TaskInstance;
import dev.jianmu.task.repository.*;
import dev.jianmu.task.service.InstanceDomainService;
import dev.jianmu.task.service.InstanceParameterDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final DefinitionRepository definitionRepository;
    private final InstanceDomainService instanceDomainService;
    private final ParameterRepository parameterRepository;
    private final ParameterReferRepository parameterReferRepository;
    private final InstanceParameterRepository instanceParameterRepository;
    private final InputParameterRepository inputParameterRepository;

    @Inject
    public TaskInstanceApplication(
            TaskInstanceRepository taskInstanceRepository,
            DefinitionRepository definitionRepository,
            InstanceDomainService instanceDomainService,
            ParameterRepository parameterRepository,
            ParameterReferRepository parameterReferRepository,
            InstanceParameterRepository instanceParameterRepository,
            InputParameterRepository inputParameterRepository
    ) {
        this.taskInstanceRepository = taskInstanceRepository;
        this.definitionRepository = definitionRepository;
        this.instanceDomainService = instanceDomainService;
        this.parameterRepository = parameterRepository;
        this.parameterReferRepository = parameterReferRepository;
        this.instanceParameterRepository = instanceParameterRepository;
        this.inputParameterRepository = inputParameterRepository;
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
    public void create(
            String businessId,
            String workflowRef,
            String workflowVersion,
            String projectId,
            String asyncTaskRef,
            String asyncTaskType
    ) {
        // 创建任务实例
        String[] strings = asyncTaskType.split(":");
        Definition definition = this.definitionRepository.findByRefAndVersion(strings[0], strings[1])
                .orElseThrow(() -> new DataNotFoundException("未找到任务定义"));
        List<TaskInstance> taskInstances = this.taskInstanceRepository.findByAsyncTaskRefAndBusinessId(asyncTaskRef, businessId);
        TaskInstance taskInstance = this.instanceDomainService.create(taskInstances, definition, businessId, projectId, asyncTaskRef, workflowRef, workflowVersion);

        // 查询流程定义参数关联
        var refers = this.parameterReferRepository
                .findByRefAndVersionAndTargetTaskRef(workflowRef, workflowVersion, taskInstance.getAsyncTaskRef());
        // 查询关联输出参数
        var instanceOutputParameters = refers.stream().map(refer -> {
            var instanceParameter = this.instanceParameterRepository
                    .findInputParamByBusinessIdAndTaskRefAndRefAndMaxSerial(businessId, refer.getSourceTaskRef(), refer.getSourceParameterRef())
                    .orElseThrow(() -> new DataNotFoundException("未找到关联的任务输出参数"));
            return Map.entry(refer.getTargetParameterRef(), instanceParameter);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        // 查询输入参数
        var inputParameters = this.inputParameterRepository
                .findByWorkflowRefAndWorkflowVersionAndAsyncTaskRef(workflowRef, workflowVersion, asyncTaskRef);
        // 任务输入参数与关联输出参数的参数值覆盖
        var taskInputParameters = definition.getInputParametersWith(inputParameters, instanceOutputParameters);

        // 创建任务实例输入参数
        var instanceInputParameters = InstanceParameterDomainService
                .createInstanceParameters(taskInputParameters, taskInstance);

        // 保存任务实例输入参数
        this.instanceParameterRepository.addAll(instanceInputParameters);
        // 保存任务实例
        this.taskInstanceRepository.add(taskInstance);
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
                            .projectId(taskInstance.getProjectId())
                            .ref(taskParameter.getRef())
                            .type(InstanceParameter.Type.OUTPUT)
                            .parameterId(parameter.getId())
                            .build();
                    return Map.entry(instanceParameter, parameter);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
