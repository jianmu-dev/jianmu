package dev.jianmu.parameter.repository;

import dev.jianmu.parameter.aggregate.Parameter;

import java.util.List;
import java.util.Set;

/**
 * @class: ParameterRepository
 * @description: 参数仓储
 * @author: Ethan Liu
 * @create: 2021-04-09 14:25
 **/
public interface ParameterRepository {
    void addAll(List<Parameter> parameters);

    List<Parameter> findByIds(Set<String> ids);
}
