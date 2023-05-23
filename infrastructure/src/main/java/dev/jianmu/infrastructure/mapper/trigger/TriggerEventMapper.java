package dev.jianmu.infrastructure.mapper.trigger;

import dev.jianmu.trigger.event.TriggerEvent;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

/**
 * @class TriggerEventMapper
 * @description TriggerEventMapper
 * @author Ethan Liu
 * @create 2021-11-11 09:44
 */
public interface TriggerEventMapper {
    @Select("SELECT * FROM `jm_trigger_event` WHERE id = #{id}")
    @Result(column = "project_id", property = "projectId")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "web_request_id", property = "webRequestId")
    @Result(column = "trigger_type", property = "triggerType")
    @Result(column = "occurred_time", property = "occurredTime")
    Optional<TriggerEvent> findById(String id);

    @Insert("insert into jm_trigger_event(id, project_id, trigger_id, web_request_id, trigger_type, occurred_time) " +
            "values(#{id}, #{projectId}, #{triggerId}, #{webRequestId}, #{triggerType}, #{occurredTime})")
    void save(TriggerEvent triggerEvent);

    @Delete("DELETE FROM jm_trigger_event where trigger_id = #{triggerId}")
    void deleteByTriggerId(String triggerId);

    @Delete("delete t1, t2 from jm_trigger_event t1 " +
            "left join jm_web_request t2 on t1.web_request_id = (t2.id collate utf8mb4_0900_ai_ci) " +
            "where t1.id = #{triggerId}")
    void deleteEventAdnWebRequestByTriggerId(String triggerId);

    @Delete("delete t1, t2 from jm_trigger_event t1 " +
        "left join jm_web_request t2 on t1.web_request_id = (t2.id collate utf8mb4_0900_ai_ci) " +
        "where t1.project_id = #{projectId}")
    void deleteByProjectId(String projectId);
}
