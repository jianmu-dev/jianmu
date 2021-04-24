package dev.jianmu.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.application.exception.DataNotFoundException;
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

    @Inject
    public TaskInstanceApplication(
            TaskInstanceRepository taskInstanceRepository,
            DefinitionRepository definitionRepository,
            InstanceDomainService instanceDomainService,
            TaskDefinitionRepository taskDefinitionRepository,
            TaskDefinitionVersionRepository taskDefinitionVersionRepository,
            ParameterRepository parameterRepository,
            ReferenceRepository referenceRepository
    ) {
        this.taskInstanceRepository = taskInstanceRepository;
        this.definitionRepository = definitionRepository;
        this.instanceDomainService = instanceDomainService;
        this.taskDefinitionRepository = taskDefinitionRepository;
        this.taskDefinitionVersionRepository = taskDefinitionVersionRepository;
        this.parameterRepository = parameterRepository;
        this.referenceRepository = referenceRepository;
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
            var parameterMap = this.handleOutputParameter(resultFile, definition.getOutputParameters());
            // TODO 需要创建参数关联
            this.parameterRepository.addAll(new ArrayList<>(parameterMap.values()));
        }
        taskInstance.executeSucceeded(resultFile);
        this.taskInstanceRepository.updateStatus(taskInstance);
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

    private Map<TaskParameter, Parameter<?>> handleOutputParameter(String resultFile, Set<TaskParameter> outputParameter) {
        if (resultFile == null || resultFile.isBlank()) {
            throw new RuntimeException("任务结果文件为空");
        }
        var objectMapper = new ObjectMapper();
        try {
            Map<String, Object> parameterMap = objectMapper.readValue(resultFile, new TypeReference<>() {
            });
            return outputParameter.stream()
                    .filter(taskParameter -> parameterMap.get(taskParameter.getRef()) != null)
                    .map(taskParameter -> {
                        var value = parameterMap.get(taskParameter.getRef());
                        return Map.entry(taskParameter, Parameter.Type.valueOf(taskParameter.getType()).newParameter(value));
                    })
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } catch (JsonProcessingException e) {
            logger.error("Json转换", e);
            throw new RuntimeException("任务结果文件格式错误");
        }
    }
}
