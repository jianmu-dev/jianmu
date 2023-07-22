package dev.jianmu.infrastructure.mapper.trigger;

import dev.jianmu.trigger.event.TriggerEventParameter;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Ethan Liu
 * @class TriggerEventParameterMapper
 * @description TriggerEventParameterMapper
 * @create 2021-11-11 09:45
 */
public interface TriggerEventParameterMapper {
    @Select("SELECT * FROM `jianmu_trigger_event_parameter` WHERE trigger_event_id = #{triggerEventId}")
    @Result(column = "parameter_id", property = "parameterId")
    List<TriggerEventParameter> findById(String triggerEventId);

    @Insert("insert into jianmu_trigger_event_parameter(trigger_event_id, name, type, value, parameter_id) " +
        "values(#{triggerEventId}, #{ep.name}, #{ep.type}, #{ep.value}, #{ep.parameterId})")
    void save(@Param("triggerEventId") String triggerEventId, @Param("ep") TriggerEventParameter triggerEventParameter);

    @Delete("delete t1, t2 from jianmu_trigger_event_parameter t1 " +
        "left join parameter t2 on t1.parameter_id = (t2.id collate utf8mb4_0900_ai_ci) " +
        "where t1.trigger_event_id = #{triggerId}")
    void deleteByTriggerId(String triggerId);

    @Delete("<script>" +
        "delete from jianmu_trigger_event_parameter " +
        "where `trigger_event_id` IN <foreach collection='triggerIds' item='item' open='(' separator=',' close=')'> #{item}</foreach>" +
        "</script>")
    void deleteParameterByTriggerIdIn(@Param("triggerIds") List<String> triggerIds);

    @Select("<script>" +
        "select parameter_id from jianmu_trigger_event_parameter " +
        "where `trigger_event_id` IN <foreach collection='triggerIds' item='item' open='(' separator=',' close=')'> #{item}</foreach>" +
        "</script>")
    List<String> findParameterIdByTriggerIdIn(@Param("triggerIds") List<String> triggerIds);
}
