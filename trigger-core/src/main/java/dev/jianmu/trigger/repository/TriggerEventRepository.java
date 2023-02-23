package dev.jianmu.trigger.repository;

import dev.jianmu.trigger.event.TriggerEvent;

import java.util.Optional;

/**
 * @class TriggerEventRepository
 * @description TriggerEventRepository
 * @author Ethan Liu
 * @create 2021-11-11 08:34
 */
public interface TriggerEventRepository {
    Optional<TriggerEvent> findById(String id);

    void save(TriggerEvent triggerEvent);

    void deleteByTriggerId(String triggerId);

    void deleteParameterByTriggerId(String triggerId);
}
