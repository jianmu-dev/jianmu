package dev.jianmu.infrastructure.repositoryimpl.parameter;

import dev.jianmu.infrastructure.mapper.parameter.ParameterDefinitionMapper;
import dev.jianmu.parameter.aggregate.ParameterDefinition;
import dev.jianmu.parameter.repository.ParameterDefinitionRepository;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.List;

/**
 * @class: DefinitionRepositoryImpl
 * @description: 参数定义仓储接口实现
 * @author: Ethan Liu
 * @create: 2021-04-04 11:13
 **/
@Repository
public class ParameterDefinitionRepositoryImpl implements ParameterDefinitionRepository {
    private final ParameterDefinitionMapper parameterDefinitionMapper;

    @Inject
    public ParameterDefinitionRepositoryImpl(ParameterDefinitionMapper parameterDefinitionMapper) {
        this.parameterDefinitionMapper = parameterDefinitionMapper;
    }

    @Override
    public void add(ParameterDefinition<?> parameterDefinition) {
        this.parameterDefinitionMapper.add(parameterDefinition);
    }

    @Override
    public void addList(List<ParameterDefinition<?>> parameterDefinitions) {
        this.parameterDefinitionMapper.addList(parameterDefinitions);
    }

    @Override
    public void deleteByBusinessId(String businessId) {
        this.parameterDefinitionMapper.deleteByBusinessId(businessId);
    }

    @Override
    public List<? extends ParameterDefinition<?>> findByBusinessIdAndScope(String businessId, String scope) {
        return this.parameterDefinitionMapper.findByBusinessIdAndScope(businessId, scope);
    }
}
