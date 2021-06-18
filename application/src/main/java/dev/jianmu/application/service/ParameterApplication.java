package dev.jianmu.application.service;

import dev.jianmu.parameter.aggregate.Parameter;
import dev.jianmu.parameter.aggregate.Reference;
import dev.jianmu.parameter.aggregate.SecretParameter;
import dev.jianmu.parameter.repository.ParameterRepository;
import dev.jianmu.parameter.repository.ReferenceRepository;
import dev.jianmu.parameter.service.ParameterDomainService;
import dev.jianmu.parameter.service.ReferenceDomainService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @class: ParameterApplication
 * @description: 参数门面类
 * @author: Ethan Liu
 * @create: 2021-04-07 16:53
 **/
@Service
public class ParameterApplication {
    private final ParameterDomainService parameterDomainService;
    private final ReferenceDomainService referenceDomainService;

    private final ParameterRepository parameterRepository;
    private final ReferenceRepository referenceRepository;

    @Inject
    public ParameterApplication(
            ParameterDomainService parameterDomainService,
            ReferenceDomainService referenceDomainService,
            ParameterRepository parameterRepository,
            ReferenceRepository referenceRepository
    ) {
        this.parameterDomainService = parameterDomainService;
        this.referenceDomainService = referenceDomainService;
        this.parameterRepository = parameterRepository;
        this.referenceRepository = referenceRepository;
    }

    public List<Parameter> findParameters(Set<String> ids) {
        var parameters = this.parameterRepository.findByIds(ids);
        return parameters.stream().filter(parameter -> (!(parameter instanceof SecretParameter))).collect(Collectors.toList());
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
}
