package dev.jianmu.infrastructure.mapper.eventbrdige;

import dev.jianmu.eventbridge.aggregate.EventParameter;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

/**
 * @class: TargetEventParameterMapper
 * @description: TargetEventParameterMapper
 * @author: Ethan Liu
 * @create: 2021-08-16 16:23
 **/
public interface TargetEventParameterMapper {
    @Select("SELECT * FROM `eb_target_event_parameter` WHERE target_event_id = #{targetEventId}")
    @Result(column = "parameter_id", property = "parameterId")
    Set<EventParameter> findById(String targetEventId);

    @Insert("insert into eb_target_event_parameter(target_event_id, name, type, parameter_id) " +
            "values(#{targetEventId}, #{ep.name}, #{ep.type}, #{ep.parameterId})")
    void save(@Param("targetEventId") String targetEventId, @Param("ep") EventParameter eventParameter);
}
