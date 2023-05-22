package dev.jianmu.trigger.repository;

import dev.jianmu.trigger.event.TriggerEvent;

import java.util.List;
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

    void deleteByProjectId(String projectId);

    void deleteParameterByTriggerIdIn(List<String> triggerIds);

    List<String> findParameterIdByTriggerIdIn(List<String> triggerIds);
}
