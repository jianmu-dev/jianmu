package dev.jianmu.trigger.repository;

import dev.jianmu.trigger.entity.TriggerEntity;

import java.util.List;
import java.util.Optional;

/**
 * @class: TriggerRepository
 * @description: TriggerRepository
 * @author: Ethan Liu
 * @create: 2021-05-24 10:26
 **/
public interface TriggerEntityRepository {
    void add(TriggerEntity triggerEntity);

    void deleteByTriggerId(String triggerId);

    Optional<TriggerEntity> findByTriggerId(String triggerId);

    List<TriggerEntity> findAll();
}
