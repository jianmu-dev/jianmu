package dev.jianmu.externalParameter.repository;

import dev.jianmu.externalParameter.aggregate.ExternalParameterLabel;

import java.util.List;
import java.util.Optional;

/**
 * @author huangxi
 * @class ExternalParameterLabelRepository
 * @description ExternalParameterLabelRepository
 * @create 2022-07-13 13:40
 */
public interface ExternalParameterLabelRepository {

    void add(ExternalParameterLabel externalParameterLabel);

    List<ExternalParameterLabel> findAll();

    Optional<ExternalParameterLabel> findByValue(String value);
}
