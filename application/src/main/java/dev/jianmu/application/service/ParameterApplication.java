package dev.jianmu.application.service;

import dev.jianmu.parameter.repository.ParameterDefinitionRepository;
import dev.jianmu.parameter.repository.ParameterInstanceRepository;
import dev.jianmu.parameter.service.ParameterDomainService;
import dev.jianmu.task.aggregate.TaskInstance;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Map;

/**
 * @class: ParameterApplication
 * @description: 参数门面类
 * @author: Ethan Liu
 * @create: 2021-04-07 16:53
 **/
@Service
public class ParameterApplication {
    private final ParameterDefinitionRepository parameterDefinitionRepository;
    private final ParameterInstanceRepository parameterInstanceRepository;
    private final ParameterDomainService parameterDomainService;

    // Scope常量
    private static final String TaskInputParameterScope = "TaskInput";
    private static final String WorkerParameterScope = "Worker";

    @Inject
    public ParameterApplication(ParameterDefinitionRepository parameterDefinitionRepository, ParameterInstanceRepository parameterInstanceRepository, ParameterDomainService parameterDomainService) {
        this.parameterDefinitionRepository = parameterDefinitionRepository;
        this.parameterInstanceRepository = parameterInstanceRepository;
        this.parameterDomainService = parameterDomainService;
    }

    public Pair<Map<String, String>, Map<String, String>> findTaskParameters(String instanceId) {
        // TODO 目前这里写死了worker,需要改成动态
        var workerDefinitions = this.parameterDefinitionRepository
                .findByBusinessIdAndScope("worker9527", WorkerParameterScope);
        var taskParameterInstance = this.parameterInstanceRepository
                .findByBusinessIdAndScope(instanceId, TaskInputParameterScope);
        var systemParameterMap = this.parameterDomainService
                .mergeSystemParameterMap(workerDefinitions, taskParameterInstance);
        var businessParameterMap = this.parameterDomainService
                .mergeBusinessParameterMap(workerDefinitions, taskParameterInstance);

        return Pair.of(systemParameterMap, businessParameterMap);
    }
}
