package dev.jianmu.infrastructure.mybatis.trigger;

import dev.jianmu.infrastructure.mapper.trigger.TriggerEventMapper;
import dev.jianmu.infrastructure.mapper.trigger.TriggerEventParameterMapper;
import dev.jianmu.trigger.event.TriggerEvent;
import dev.jianmu.trigger.repository.TriggerEventRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @class TriggerEventRepositoryImpl
 * @description TriggerEventRepositoryImpl
 * @author Ethan Liu
 * @create 2021-11-11 08:36
 */
@Repository
public class TriggerEventRepositoryImpl implements TriggerEventRepository {
    private final TriggerEventMapper triggerEventMapper;
    private final TriggerEventParameterMapper triggerEventParameterMapper;

    public TriggerEventRepositoryImpl(
            TriggerEventMapper triggerEventMapper,
            TriggerEventParameterMapper triggerEventParameterMapper
    ) {
        this.triggerEventMapper = triggerEventMapper;
        this.triggerEventParameterMapper = triggerEventParameterMapper;
    }

    @Override
    public Optional<TriggerEvent> findById(String id) {
        return this.triggerEventMapper.findById(id).map(triggerEvent -> {
            var parameters = this.triggerEventParameterMapper.findById(triggerEvent.getId());
            triggerEvent.setParameters(parameters);
            return triggerEvent;
        });
    }

    @Override
    public void save(TriggerEvent triggerEvent) {
        this.triggerEventMapper.save(triggerEvent);
        triggerEvent.getParameters().forEach(parameter -> {
            this.triggerEventParameterMapper.save(triggerEvent.getId(), parameter);
        });
    }

    @Override
    public void deleteByTriggerId(String triggerId) {
        this.triggerEventMapper.deleteByTriggerId(triggerId);
    }

    @Override
    public void deleteParameterByTriggerId(String triggerId) {
        this.triggerEventParameterMapper.deleteByTriggerId(triggerId);
    }
}
