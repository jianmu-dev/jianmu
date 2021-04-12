package dev.jianmu.parameter.service;

import dev.jianmu.parameter.aggregate.Reference;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @class: ReferenceDomainService
 * @description: 参数引用领域服务
 * @author: Ethan Liu
 * @create: 2021-04-10 09:21
 **/
public class ReferenceDomainService {

    public Set<Reference> createReferences(String contextId, String parameterId, Set<String> linkedParameterIds) {
        return linkedParameterIds.stream()
                .map(id ->
                        Reference.Builder.aReference()
                                .contextId(contextId)
                                .linkedParameterId(id)
                                .parameterId(parameterId)
                                .build()
                )
                .collect(Collectors.toSet());
    }

    public Map<String, String> calculateIds(Map<String, String> parameterMap, List<Reference> references) {
        // 创建参数ID关联Map
        Map<String, String> referenceMap = references.stream()
                .map(reference -> Map.entry(reference.getLinkedParameterId(), reference.getParameterId()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        // 根据传入的参数ID查找最终参数ID
        return parameterMap.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), this.findLastId(referenceMap, entry.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private String findLastId(Map<String, String> aMap, String parameterId) {
        var id = aMap.get(parameterId);
        if (id != null) {
            return findLastId(aMap, id);
        } else {
            return parameterId;
        }
    }
}
