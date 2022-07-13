package dev.jianmu.infrastructure.mapper.trigger;

import dev.jianmu.infrastructure.typehandler.WebhookTypeHandler;
import dev.jianmu.trigger.aggregate.Trigger;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * @class TriggerMapper
 * @description TriggerMapper
 * @author Ethan Liu
 * @create 2021-05-25 15:29
*/
public interface TriggerMapper {
    @Insert("insert into jm_trigger(id, project_id, type, schedule, webhook) " +
            "values(#{id}, #{projectId}, #{type}, #{schedule}, #{webhook, jdbcType=BLOB, typeHandler=dev.jianmu.infrastructure.typehandler.WebhookTypeHandler})")
    void add(Trigger trigger);

    @Update("update jm_trigger set project_id = #{projectId}, type = #{type}, schedule = #{schedule}, webhook = #{webhook, jdbcType=BLOB, typeHandler=dev.jianmu.infrastructure.typehandler.WebhookTypeHandler} where id = #{id}")
    void updateById(Trigger trigger);

    @Delete("delete from jm_trigger where id = #{id}")
    void deleteById(String id);

    @Select("SELECT * FROM `jm_trigger` WHERE project_id = #{projectId}")
    @Result(column = "project_id", property = "projectId")
    @Result(column = "webhook", property = "webhook", typeHandler = WebhookTypeHandler.class)
    Optional<Trigger> findByProjectId(String projectId);

    @Select("SELECT * FROM `jm_trigger` WHERE id = #{triggerId}")
    @Result(column = "project_id", property = "projectId")
    @Result(column = "webhook", property = "webhook", typeHandler = WebhookTypeHandler.class)
    Optional<Trigger> findByTriggerId(String triggerId);

    @Select("SELECT * FROM `jm_trigger` WHERE type = #{type}")
    @Result(column = "project_id", property = "projectId")
    @Result(column = "webhook", property = "webhook", typeHandler = WebhookTypeHandler.class)
    List<Trigger> findAllByType(Trigger.Type type);
}
