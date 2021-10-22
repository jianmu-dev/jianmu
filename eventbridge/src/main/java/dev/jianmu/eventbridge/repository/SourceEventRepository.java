package dev.jianmu.eventbridge.repository;

import dev.jianmu.eventbridge.aggregate.event.SourceEvent;

/**
 * @class: SourceEventRepository
 * @description: SourceEventRepository
 * @author: Ethan Liu
 * @create: 2021-10-17 09:43
 **/
public interface SourceEventRepository {
    @Insert("insert into eb_target_event(id, source_id, source_event_id, connection_event_id, target_id, target_ref, destination_id, payload, occurred_time) " +
            "values(#{id}, #{sourceId}, #{sourceEventId}, #{connectionEventId}, #{targetId}, #{targetRef}, #{destinationId}, #{payload}, #{occurredTime})")
    void add(SourceEvent sourceEvent);
}
