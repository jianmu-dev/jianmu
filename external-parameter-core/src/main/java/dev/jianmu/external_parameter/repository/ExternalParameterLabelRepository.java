package dev.jianmu.external_parameter.repository;

import dev.jianmu.external_parameter.aggregate.ExternalParameterLabel;

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

    void deleteByAssociationIdAndType(String associationId, String associationType, String associationPlatform);

    List<ExternalParameterLabel> findAll(String id, String type, String associationPlatform);

    Optional<ExternalParameterLabel> findByValue(String id, String type, String associationPlatform, String value);
}
