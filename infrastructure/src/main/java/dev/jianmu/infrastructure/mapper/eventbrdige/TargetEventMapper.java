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
    @Result(column = "target_id", property = "targetId")
    @Result(column = "target_ref", property = "targetRef")
    @Result(column = "destination_id", property = "destinationId")
    Optional<TargetEvent> findById(String id);

    @Insert("insert into eb_target_event(id, source_id, target_id, target_ref, destination_id) " +
            "values(#{id}, #{sourceId}, #{targetId}, #{targetRef}, #{destinationId})")
    void save(TargetEvent targetEvent);
}
