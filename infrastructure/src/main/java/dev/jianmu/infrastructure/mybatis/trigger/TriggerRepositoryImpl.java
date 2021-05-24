package dev.jianmu.infrastructure.mybatis.trigger;

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
    @Override
    public void add(TriggerEntity triggerEntity) {

    }

    @Override
    public void deleteByTriggerId(String triggerId) {

    }

    @Override
    public Optional<TriggerEntity> findByTriggerId(String triggerId) {
        return Optional.empty();
    }

    @Override
    public List<TriggerEntity> findAll() {
        return null;
    }
}
