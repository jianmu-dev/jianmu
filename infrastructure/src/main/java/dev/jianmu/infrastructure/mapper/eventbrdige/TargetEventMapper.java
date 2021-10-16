package dev.jianmu.infrastructure.mapper.eventbrdige;

import dev.jianmu.eventbridge.aggregate.TargetEvent;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

/**
 * @class: TargetEventMapper
 * @description: TargetEventMapper
 * @author: Ethan Liu
 * @create: 2021-08-16 16:17
 **/
public interface TargetEventMapper {
    @Select("SELECT * FROM `eb_target_event` WHERE id = #{id}")
    @Result(column = "source_id", property = "sourceId")
    @Result(column = "source_event_id", property = "sourceEventId")
    @Result(column = "connection_event_id", property = "connectionEventId")
    @Result(column = "target_id", property = "targetId")
    @Result(column = "target_ref", property = "targetRef")
    @Result(column = "destination_id", property = "destinationId")
    Optional<TargetEvent> findById(String id);

    @Insert("insert into eb_target_event(id, source_id, source_event_id, connection_event_id, target_id, target_ref, destination_id, payload) " +
            "values(#{id}, #{sourceId}, #{sourceEventId}, #{connectionEventId}, #{targetId}, #{targetRef}, #{destinationId}, #{payload})")
    void save(TargetEvent targetEvent);
}
