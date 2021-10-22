package dev.jianmu.infrastructure.mapper.eventbrdige;

import dev.jianmu.eventbridge.aggregate.event.SourceEvent;
import org.apache.ibatis.annotations.Insert;

/**
 * @class: SourceEventMapper
 * @description: SourceEventMapper
 * @author: Ethan Liu
 * @create: 2021-10-22 08:42
 **/
public interface SourceEventMapper {
    @Insert("insert into eb_source_event(id, source_id, bridge_id, source_type, payload, occurred_time) " +
            "values(#{id}, #{sourceId}, #{bridgeId}, #{sourceType}, #{payload}, #{occurredTime})")
    void add(SourceEvent sourceEvent);
}
