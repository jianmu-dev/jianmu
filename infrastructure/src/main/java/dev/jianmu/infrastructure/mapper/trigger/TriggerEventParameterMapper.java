package dev.jianmu.infrastructure.mapper.trigger;

import dev.jianmu.trigger.event.TriggerEventParameter;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @class TriggerEventParameterMapper
 * @description TriggerEventParameterMapper
 * @author Ethan Liu
 * @create 2021-11-11 09:45
 */
public interface TriggerEventParameterMapper {
    @Select("SELECT * FROM `jm_trigger_event_parameter` WHERE trigger_event_id = #{triggerEventId}")
    @Result(column = "parameter_id", property = "parameterId")
    List<TriggerEventParameter> findById(String triggerEventId);

    @Insert("insert into jm_trigger_event_parameter(trigger_event_id, name, type, value, parameter_id) " +
            "values(#{triggerEventId}, #{ep.name}, #{ep.type}, #{ep.value}, #{ep.parameterId})")
    void save(@Param("triggerEventId") String triggerEventId, @Param("ep") TriggerEventParameter triggerEventParameter);
}
