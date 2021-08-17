package dev.jianmu.infrastructure.mybatis.eventbridge;

import dev.jianmu.eventbridge.aggregate.TargetEvent;
import dev.jianmu.eventbridge.repository.TargetEventRepository;
import dev.jianmu.infrastructure.mapper.eventbrdige.TargetEventMapper;
import dev.jianmu.infrastructure.mapper.eventbrdige.TargetEventParameterMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @class: TargetEventRepositoryImpl
 * @description: TargetEventRepositoryImpl
 * @author: Ethan Liu
 * @create: 2021-08-14 19:39
 **/
@Repository
public class TargetEventRepositoryImpl implements TargetEventRepository {
    private final TargetEventMapper targetEventMapper;
    private final TargetEventParameterMapper targetEventParameterMapper;

    public TargetEventRepositoryImpl(TargetEventMapper targetEventMapper, TargetEventParameterMapper targetEventParameterMapper) {
        this.targetEventMapper = targetEventMapper;
        this.targetEventParameterMapper = targetEventParameterMapper;
    }

    @Override
    public Optional<TargetEvent> findById(String id) {
        return this.targetEventMapper.findById(id).map(targetEvent -> {
            var eventParameters = this.targetEventParameterMapper.findById(targetEvent.getId());
            targetEvent.setEventParameters(eventParameters);
            return targetEvent;
        });
    }

    @Override
    public void save(TargetEvent targetEvent) {
        this.targetEventMapper.save(targetEvent);
        targetEvent.getEventParameters().forEach(eventParameter -> {
            this.targetEventParameterMapper.save(targetEvent.getId(), eventParameter);
        });
    }
}
