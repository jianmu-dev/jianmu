package dev.jianmu.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.dsl.repository.OutputParameterReferRepository;
import dev.jianmu.parameter.aggregate.Parameter;
import dev.jianmu.parameter.repository.ParameterRepository;
import dev.jianmu.parameter.repository.ReferenceRepository;
import dev.jianmu.task.aggregate.Definition;
import dev.jianmu.task.aggregate.TaskInstance;
import dev.jianmu.task.aggregate.TaskParameter;
import dev.jianmu.task.repository.DefinitionRepository;
import dev.jianmu.task.repository.TaskInstanceRepository;
import dev.jianmu.task.service.InstanceDomainService;
import dev.jianmu.version.aggregate.TaskDefinition;
import dev.jianmu.version.aggregate.TaskDefinitionVersion;
import dev.jianmu.version.repository.TaskDefinitionRepository;
import dev.jianmu.version.repository.TaskDefinitionVersionRepository;
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
    private final DefinitionRepository definitionRepository;
    private final InstanceDomainService instanceDomainService;
    private final TaskDefinitionRepository taskDefinitionRepository;
    private final TaskDefinitionVersionRepository taskDefinitionVersionRepository;
    private final ParameterRepository parameterRepository;
    private final ReferenceRepository referenceRepository;
    private final OutputParameterReferRepository outputParameterReferRepository;

    @Inject
    public TaskInstanceApplication(
            TaskInstanceRepository taskInstanceRepository,
            DefinitionRepository definitionRepository,
            InstanceDomainService instanceDomainService,
            TaskDefinitionRepository taskDefinitionRepository,
            TaskDefinitionVersionRepository taskDefinitionVersionRepository,
            ParameterRepository parameterRepository,
            ReferenceRepository referenceRepository,
            OutputParameterReferRepository outputParameterReferRepository
    ) {
        this.taskInstanceRepository = taskInstanceRepository;
        this.definitionRepository = definitionRepository;
        this.instanceDomainService = instanceDomainService;
        this.taskDefinitionRepository = taskDefinitionRepository;
        this.taskDefinitionVersionRepository = taskDefinitionVersionRepository;
        this.parameterRepository = parameterRepository;
        this.referenceRepository = referenceRepository;
        this.outputParameterReferRepository = outputParameterReferRepository;
    }

    public List<TaskInstance> findByBusinessId(String businessId) {
        return this.taskInstanceRepository.findByBusinessId(businessId);
    }

    public Optional<TaskDefinitionVersion> findByDefKey(String defKey) {
        return this.taskDefinitionVersionRepository.findByDefinitionKey(defKey);
    }

    public Optional<TaskDefinition> findByRef(String ref) {
        return this.taskDefinitionRepository.findByRef(ref);
    }

    @Transactional
    public void create(String businessId, String triggerId, String asyncTaskRef, String asyncTaskType) {
        // 创建任务实例
        Definition definition = this.definitionRepository.findByKey(asyncTaskType)
                .orElseThrow(() -> new DataNotFoundException("未找到任务定义"));
        List<TaskInstance> taskInstances = this.taskInstanceRepository.findByAsyncTaskRefAndBusinessId(asyncTaskRef, businessId);
        TaskInstance taskInstance = this.instanceDomainService.create(taskInstances, definition, businessId, triggerId, asyncTaskRef);
        this.taskInstanceRepository.add(taskInstance);
    }

    @Transactional
    public void executeSucceeded(String taskInstanceId, String resultFile) {
        TaskInstance taskInstance = this.taskInstanceRepository.findById(taskInstanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该任务实例"));
        var definition = this.definitionRepository.findByKey(taskInstance.getDefKey())
                .orElseThrow(() -> new DataNotFoundException("未找到该任务定义"));
        if (definition.getResultFile() != null) {
            // 解析Json为Map
            var parameterMap = this.parseJson(resultFile);
            // 查找需赋值的输出参数
            var taskParameters = taskInstance.checkOutputParameters(parameterMap);
            // 任务实例输出参数类型创建参数
            var outputParameters = this.handleOutputParameter(parameterMap, taskParameters);
            // 更新输出参数的参数ID
            taskInstance.updateOutputParameters(outputParameters.keySet());
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

    private Map<TaskParameter, Parameter<?>> handleOutputParameter(Map<String, Object> parameterMap, Set<TaskParameter> outputParameter) {
        return outputParameter.stream()
                .map(taskParameter -> {
                    var value = parameterMap.get(taskParameter.getRef());
                    // 创建参数
                    var parameter = Parameter.Type.valueOf(taskParameter.getType()).newParameter(value);
                    // 更新任务输出参数ID
                    taskParameter.setParameterId(parameter.getId());
                    return Map.entry(taskParameter, parameter);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
