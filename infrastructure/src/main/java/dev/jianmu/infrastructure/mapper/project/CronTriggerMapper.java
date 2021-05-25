package dev.jianmu.infrastructure.mapper.project;

import dev.jianmu.project.aggregate.CronTrigger;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

/**
 * @class: CronTriggerMapper
 * @description: CronTriggerMapper
 * @author: Ethan Liu
 * @create: 2021-05-25 15:32
 **/
public interface CronTriggerMapper {
    @Insert("insert into cron_trigger(id, project_id, corn) " +
            "values(#{id}, #{projectId}, #{corn})")
    void add(CronTrigger cronTrigger);

    @Delete("delete from cron_trigger where project_id = #{projectId}")
    void deleteByProjectId(String projectId);

    @Delete("delete from cron_trigger where id = #{id}")
    void deleteById(String id);

    @Select("SELECT * FROM `cron_trigger` WHERE id = #{id}")
    @Result(column = "project_id", property = "projectId")
    Optional<CronTrigger> findById(String id);

    @Select("SELECT * FROM `cron_trigger` WHERE project_id = #{projectId}")
    @Result(column = "project_id", property = "projectId")
    List<CronTrigger> findByProjectId(String projectId);
}
