package dev.jianmu.infrastructure.mybatis.eventbridge;

import dev.jianmu.eventbridge.aggregate.event.SourceEvent;
import dev.jianmu.eventbridge.repository.SourceEventRepository;
import dev.jianmu.infrastructure.mapper.eventbrdige.SourceEventMapper;
import org.springframework.stereotype.Repository;

/**
 * @class: SourceEventRepositoryImpl
 * @description: SourceEventRepositoryImpl
 * @author: Ethan Liu
 * @create: 2021-10-22 03:50
 **/
@Repository
public class SourceEventRepositoryImpl implements SourceEventRepository {
    private final SourceEventMapper sourceEventMapper;

    public SourceEventRepositoryImpl(SourceEventMapper sourceEventMapper) {
        this.sourceEventMapper = sourceEventMapper;
    }

    @Override
    public void add(SourceEvent sourceEvent) {
        this.sourceEventMapper.add(sourceEvent);
    }
}
