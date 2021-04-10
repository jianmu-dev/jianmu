package dev.jianmu.parameter.repository;

import dev.jianmu.parameter.aggregate.Reference;

import java.util.List;
import java.util.Set;

/**
 * @class: ReferenceRepository
 * @description: 参数引用仓储定义
 * @author: Ethan Liu
 * @create: 2021-04-09 18:55
 **/
public interface ReferenceRepository {
    void addAll(List<Reference> references);

    List<Reference> findByLinkedParameterIds(Set<String> linkedParameterIds);
}
