package dev.jianmu.infrastructure.mybatis.eventbridge;

import dev.jianmu.eventbridge.aggregate.Source;
import dev.jianmu.eventbridge.repository.SourceRepository;
import dev.jianmu.infrastructure.mapper.eventbrdige.SourceMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @class: SourceRepositoryImpl
 * @description: SourceRepositoryImpl
 * @author: Ethan Liu
 * @create: 2021-08-14 19:37
 **/
@Repository
public class SourceRepositoryImpl implements SourceRepository {
    private final SourceMapper sourceMapper;

    public SourceRepositoryImpl(SourceMapper sourceMapper) {
        this.sourceMapper = sourceMapper;
    }

    @Override
    public Optional<Source> findById(String id) {
        return this.sourceMapper.findById(id);
    }

    @Override
    public Optional<Source> findByBridgeId(String bridgeId) {
        return this.sourceMapper.findByBridgeId(bridgeId);
    }

    @Override
    public void updateTokenById(Source source) {
        this.sourceMapper.updateTokenById(source);
    }

    @Override
    public void saveOrUpdate(Source source) {
        this.sourceMapper.saveOrUpdate(source);
    }

    @Override
    public void deleteById(String id) {
        this.sourceMapper.deleteById(id);
    }

    @Override
    public void deleteByBridgeId(String bridgeId) {
        this.sourceMapper.deleteByBridgeId(bridgeId);
    }
}
