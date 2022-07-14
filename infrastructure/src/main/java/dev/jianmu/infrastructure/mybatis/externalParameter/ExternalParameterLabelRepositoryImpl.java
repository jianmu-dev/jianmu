package dev.jianmu.infrastructure.mybatis.externalParameter;

import dev.jianmu.externalParameter.aggregate.ExternalParameterLabel;
import dev.jianmu.externalParameter.repository.ExternalParameterLabelRepository;
import dev.jianmu.infrastructure.mapper.externalParameter.ExternalParameterLabelMapper;
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
        this.externalParameterLabelMapper.add(externalParameterLabel);
    }

    @Override
    public List<ExternalParameterLabel> findAll() {
        return this.externalParameterLabelMapper.findAll();
    }

    @Override
    public Optional<ExternalParameterLabel> findByValue(String value) {
        return this.externalParameterLabelMapper.findByValue(value);
    }
}
