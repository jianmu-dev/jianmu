package dev.jianmu.eventbridge.repository;

import dev.jianmu.eventbridge.aggregate.event.TargetEvent;

import java.util.Optional;

/**
 * @class: EventRepository
 * @description: EventRepository
 * @author: Ethan Liu
 * @create: 2021-08-11 16:08
 **/
public interface TargetEventRepository {
    Optional<TargetEvent> findById(String id);

    void save(TargetEvent targetEvent);
}
