package dev.jianmu.trigger.repository;

import dev.jianmu.trigger.aggregate.Trigger;

import java.util.List;
import java.util.Optional;

/**
 * @class TriggerRepository
 * @description TriggerRepository
 * @author Ethan Liu
 * @create 2021-11-10 11:16
 */
public interface TriggerRepository {
    void add(Trigger trigger);

    void updateById(Trigger trigger);

    void deleteById(String id);

    Optional<Trigger> findByProjectId(String projectId);

    Optional<Trigger> findByTriggerId(String triggerId);

    List<Trigger> findCronTriggerAll();

    Optional<Trigger> findByRef(String ref);
}
