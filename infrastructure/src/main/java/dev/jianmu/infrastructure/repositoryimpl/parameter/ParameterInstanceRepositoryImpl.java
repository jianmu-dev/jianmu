package dev.jianmu.infrastructure.repositoryimpl.parameter;

import dev.jianmu.infrastructure.mapper.parameter.ParameterInstanceMapper;
import dev.jianmu.parameter.aggregate.ParameterInstance;
import dev.jianmu.parameter.repository.ParameterInstanceRepository;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.List;

/**
 * @class: InstanceRepositoryImpl
 * @description: 参数实例仓储接口实现
 * @author: Ethan Liu
 * @create: 2021-04-04 11:13
 **/
@Repository
public class ParameterInstanceRepositoryImpl implements ParameterInstanceRepository {
    private final ParameterInstanceMapper parameterInstanceMapper;

    @Inject
    public ParameterInstanceRepositoryImpl(ParameterInstanceMapper parameterInstanceMapper) {
        this.parameterInstanceMapper = parameterInstanceMapper;
    }

    @Override
    public void add(ParameterInstance<?> parameterInstance) {
        this.parameterInstanceMapper.add(parameterInstance);
    }

    @Override
    public void addList(List<ParameterInstance<?>> parameterInstances) {
        this.parameterInstanceMapper.addList(parameterInstances);
    }

    @Override
    public List<? extends ParameterInstance<?>> findByBusinessIdAndScope(String businessId, String scope) {
        return this.parameterInstanceMapper.findByBusinessIdAndScope(businessId, scope);
    }
}
