package dev.jianmu.infrastructure.mybatis.externalParameter;

import dev.jianmu.external_parameter.aggregate.ExternalParameter;
import dev.jianmu.external_parameter.repository.ExternalParameterRepository;
import dev.jianmu.infrastructure.mapper.external_parameter.ExternalParameterMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author huangxi
 * @class ExternalParameterRepositoryImpl
 * @description ExternalParameterRepositoryImpl
 * @create 2022-07-13 15:22
 */

@Repository
public class ExternalParameterRepositoryImpl implements ExternalParameterRepository {
    private final ExternalParameterMapper externalParameterMapper;

    public ExternalParameterRepositoryImpl(ExternalParameterMapper externalParameterMapper) {
        this.externalParameterMapper = externalParameterMapper;
    }

    @Override
    public void add(ExternalParameter externalParameter) {
        this.externalParameterMapper.add(externalParameter);
    }

    @Override
    public void deleteById(String id) {
        this.externalParameterMapper.deleteById(id);
    }

    @Override
    public void updateById(ExternalParameter externalParameter) {
        this.externalParameterMapper.updateById(externalParameter);
    }

    @Override
    public Optional<ExternalParameter> findById(String id) {
        return this.externalParameterMapper.findById(id);
    }

    @Override
    public List<ExternalParameter> findAll(String id, String type) {
        return this.externalParameterMapper.findAll(id, type);
    }

    @Override
    public Optional<ExternalParameter> findByRef(String ref) {
        return this.externalParameterMapper.findByRef(ref);
    }
}
