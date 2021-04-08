package dev.jianmu.parameter.repository;

import dev.jianmu.parameter.aggregate.ParameterDefinition;

import java.util.List;

/**
 * @class: DefinitionRepository
 * @description: 参数定义仓储接口
 * @author: Ethan Liu
 * @create: 2021-03-25 19:32
 **/
public interface ParameterDefinitionRepository {
    void add(ParameterDefinition<?> parameterDefinition);

    void addList(List<ParameterDefinition<?>> parameterDefinitions);

    void deleteByBusinessId(String businessId);

    List<? extends ParameterDefinition<?>> findByBusinessIdAndScope(String businessId, String scope);
}
