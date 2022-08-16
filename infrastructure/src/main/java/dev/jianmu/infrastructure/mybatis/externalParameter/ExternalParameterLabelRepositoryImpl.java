package dev.jianmu.infrastructure.mybatis.externalParameter;

import dev.jianmu.external_parameter.aggregate.ExternalParameterLabel;
import dev.jianmu.external_parameter.repository.ExternalParameterLabelRepository;
import dev.jianmu.infrastructure.mapper.external_parameter.ExternalParameterLabelMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author huangxi
 * @class ExternalParameterLabelRepositoryImpl
 * @description ExternalParameterLabelRepositoryImpl
 * @create 2022-07-13 15:22
 */

@Repository
public class ExternalParameterLabelRepositoryImpl implements ExternalParameterLabelRepository {
    private final ExternalParameterLabelMapper externalParameterLabelMapper;

    public ExternalParameterLabelRepositoryImpl(ExternalParameterLabelMapper externalParameterLabelMapper) {
        this.externalParameterLabelMapper = externalParameterLabelMapper;
    }

    @Override
    public void add(ExternalParameterLabel externalParameterLabel) {
        if (externalParameterLabel.getAssociationId() == null || externalParameterLabel.getAssociationType() == null) {
            externalParameterLabel.setAssociationId("");
            externalParameterLabel.setAssociationType("");
        }
        this.externalParameterLabelMapper.add(externalParameterLabel);
    }

    @Override
    public List<ExternalParameterLabel> findAll(String id, String type) {
        return this.externalParameterLabelMapper.findAll(id, type);
    }

    @Override
    public Optional<ExternalParameterLabel> findByValue(String id, String type, String value) {
        return this.externalParameterLabelMapper.findByValue(id, type, value);
    }
}
