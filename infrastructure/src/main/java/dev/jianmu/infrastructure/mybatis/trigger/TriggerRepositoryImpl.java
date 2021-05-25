package dev.jianmu.infrastructure.mybatis.trigger;

import dev.jianmu.infrastructure.mapper.trigger.TriggerMapper;
import dev.jianmu.trigger.entity.TriggerEntity;
import dev.jianmu.trigger.repository.TriggerRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @class: TriggerRepositoryImpl
 * @description: TriggerRepositoryImpl
 * @author: Ethan Liu
 * @create: 2021-05-24 18:31
 **/
@Repository
public class TriggerRepositoryImpl implements TriggerRepository {
    private final TriggerMapper triggerMapper;

    public TriggerRepositoryImpl(TriggerMapper triggerMapper) {
        this.triggerMapper = triggerMapper;
    }

    @Override
    public void add(TriggerEntity triggerEntity) {
        this.triggerMapper.add(triggerEntity);
    }

    @Override
    public void deleteByTriggerId(String triggerId) {
        this.triggerMapper.deleteByTriggerId(triggerId);
    }

    @Override
    public Optional<TriggerEntity> findByTriggerId(String triggerId) {
        return this.triggerMapper.findByTriggerId(triggerId);
    }

    @Override
    public List<TriggerEntity> findAll() {
        return this.triggerMapper.findAll();
    }
}
