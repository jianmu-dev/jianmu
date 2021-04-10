package dev.jianmu.parameter.service;

import dev.jianmu.parameter.aggregate.Reference;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @class: ReferenceDomainService
 * @description: 参数引用领域服务
 * @author: Ethan Liu
 * @create: 2021-04-10 09:21
 **/
public class ReferenceDomainService {

    public Set<Reference> createReferences(String parameterId, Set<String> linkedParameterIds) {
        return linkedParameterIds.stream()
                .map(id ->
                        Reference.Builder.aReference()
                                .linkedParameterId(id)
                                .parameterId(parameterId)
                                .build()
                )
                .collect(Collectors.toSet());
    }

    public Set<String> calculateIds(List<Reference> references) {
        Map<String, String> referenceMap = new HashMap<>();
        Set<String> parameterIds = new HashSet<>();
        references.forEach(reference -> {
            referenceMap.put(reference.getLinkedParameterId(), reference.getParameterId());
            parameterIds.add(reference.getParameterId());
        });
        return parameterIds.stream()
                .map(parameterId -> this.findLastId(referenceMap, parameterId))
                .collect(Collectors.toSet());
    }

    private String findLastId(Map<String, String> aMap, String parameterId) {
        var id = aMap.get(parameterId);
        if (id != null) {
            return findLastId(aMap, parameterId);
        } else {
            return parameterId;
        }
    }
}
