package dev.jianmu.infrastructure.mapper.trigger;

import dev.jianmu.trigger.entity.TriggerEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

/**
 * @class: TriggerMapper
 * @description: TriggerMapper
 * @author: Ethan Liu
 * @create: 2021-05-25 15:29
 **/
public interface TriggerMapper {
    @Insert("insert into quartz_trigger(id, trigger_id, cron) " +
            "values(#{id}, #{triggerId}, #{cron})")
    void add(TriggerEntity triggerEntity);

    @Delete("delete from quartz_trigger where trigger_id = #{triggerId}")
    void deleteByTriggerId(String triggerId);

    @Select("SELECT * FROM `quartz_trigger` WHERE trigger_id = #{triggerId}")
    @Result(column = "trigger_id", property = "triggerId")
    Optional<TriggerEntity> findByTriggerId(String triggerId);

    @Select("SELECT * FROM `quartz_trigger`")
    @Result(column = "trigger_id", property = "triggerId")
    List<TriggerEntity> findAll();
}
