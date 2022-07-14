package dev.jianmu.externalParameter.repository;

import dev.jianmu.externalParameter.aggregate.ExternalParameter;

import java.util.List;
import java.util.Optional;

/**
 * @author huangxi
 * @class ExternalParameterRepository
 * @description ExternalParameterRepository
 * @create 2022-07-13 13:40
 */
public interface ExternalParameterRepository {
    void add(ExternalParameter externalParameter);

    void deleteById(String id);

    void updateById(ExternalParameter externalParameter);

    Optional<ExternalParameter> findById(String id);

    List<ExternalParameter> findAll();
}
