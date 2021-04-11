package dev.jianmu.application.service;

import dev.jianmu.parameter.aggregate.Parameter;
import dev.jianmu.parameter.aggregate.Reference;
import dev.jianmu.parameter.repository.ParameterDefinitionRepository;
import dev.jianmu.parameter.repository.ParameterInstanceRepository;
import dev.jianmu.parameter.repository.ParameterRepository;
import dev.jianmu.parameter.repository.ReferenceRepository;
import dev.jianmu.parameter.service.ParameterDomainService;
import dev.jianmu.parameter.service.ReferenceDomainService;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

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
    private final ReferenceDomainService referenceDomainService;

    private final ParameterRepository parameterRepository;
    private final ReferenceRepository referenceRepository;

    // Scope常量
    private static final String TaskInputParameterScope = "TaskInput";
    private static final String WorkerParameterScope = "Worker";

    @Inject
    public ParameterApplication(
            ParameterDefinitionRepository parameterDefinitionRepository,
            ParameterInstanceRepository parameterInstanceRepository,
            ParameterDomainService parameterDomainService,
            ReferenceDomainService referenceDomainService,
            ParameterRepository parameterRepository,
            ReferenceRepository referenceRepository
    ) {
        this.parameterDefinitionRepository = parameterDefinitionRepository;
        this.parameterInstanceRepository = parameterInstanceRepository;
        this.parameterDomainService = parameterDomainService;
        this.referenceDomainService = referenceDomainService;
        this.parameterRepository = parameterRepository;
        this.referenceRepository = referenceRepository;
    }

    // 创建参数
    public Map<String, Parameter> addParameters(Map<String, Object> parameterMap) {
        var parameters = this.parameterDomainService.createParameters(parameterMap);
        this.parameterRepository.addAll(new ArrayList<>(parameters.values()));
        return parameters;
    }

    // 创建参数引用
    public Set<Reference> addReferences(String contextId, String parameterId, Set<String> linkedParameterIds) {
        var refers = this.referenceDomainService.createReferences(contextId, parameterId, linkedParameterIds);
        this.referenceRepository.addAll(new ArrayList<>(refers));
        return refers;
    }

    // 查询参数引用
    public List<Reference> findReferences(Set<String> contextIds) {
        return this.referenceRepository.findByContextIds(contextIds);
    }

    // 查询参数引用
    public List<Reference> findReferences(String contextId) {
        return this.referenceRepository.findByContextId(contextId);
    }

    // 查询参数值
    public Map<String, Object> findParameters(Set<String> contextIds, Map<String, String> parameterMap) {
        var references = this.referenceRepository.findByContextIds(contextIds);
        var newParameterMap = this.referenceDomainService.calculateIds(parameterMap, references);
        var parameters = this.parameterRepository.findByIds(new HashSet<>(newParameterMap.values()));
        return this.parameterDomainService.createParameterMap(newParameterMap, parameters);
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
