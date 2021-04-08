package dev.jianmu.parameter.repository;

import dev.jianmu.parameter.aggregate.ParameterInstance;

import java.util.List;

/**
 * @class: InstanceRepository
 * @description: 参数实例仓储接口
 * @author: Ethan Liu
 * @create: 2021-04-04 10:22
 **/
public interface ParameterInstanceRepository {
    void add(ParameterInstance<?> parameterInstance);

    void addList(List<ParameterInstance<?>> parameterInstances);

    List<? extends ParameterInstance<?>> findByBusinessIdAndScope(String businessId, String scope);
}
