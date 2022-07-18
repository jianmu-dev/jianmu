package dev.jianmu.external_parameter.repository;

import dev.jianmu.external_parameter.aggregate.ExternalParameter;

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

    List<ExternalParameter> findAll(String id, String type);

    Optional<ExternalParameter> findByRef(String ref);
}
