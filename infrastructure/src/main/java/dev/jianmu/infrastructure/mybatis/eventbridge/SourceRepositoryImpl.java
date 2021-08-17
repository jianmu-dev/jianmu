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
    public void save(Source source) {
        this.sourceMapper.save(source);
    }
}
