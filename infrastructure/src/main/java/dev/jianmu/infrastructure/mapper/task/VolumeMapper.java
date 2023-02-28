package dev.jianmu.infrastructure.mapper.task;

import dev.jianmu.task.aggregate.Volume;
import org.apache.ibatis.annotations.*;

import java.util.Optional;

/**
 * @author <a href="https://gitee.com/ethan-liu">Ethan Liu</a>
 * @date 2023-02-19 11:10
 */
public interface VolumeMapper {

    @Insert("insert into volume(id, name, scope, project_id, worker_id, available, taint, created_time, available_time) " +
            "values(#{id}, #{name}, #{scope}, #{projectId}, #{workerId}, #{available}, #{taint}, #{createdTime}, #{availableTime})")
    void create(Volume volume);

    @Select("select * from volume where id = #{id}")
    @Result(column = "project_id", property = "projectId")
    @Result(column = "worker_id", property = "workerId")
    @Result(column = "created_time", property = "createdTime")
    @Result(column = "available_time", property = "availableTime")
    Optional<Volume> findById(String id);

    @Select("select * from volume where name = #{name}")
    @Result(column = "project_id", property = "projectId")
    @Result(column = "worker_id", property = "workerId")
    @Result(column = "created_time", property = "createdTime")
    @Result(column = "available_time", property = "availableTime")
    Optional<Volume> findByName(String name);

    @Update("update volume set available = #{available}, worker_id = #{workerId}, available_time = #{availableTime} where id = #{id}")
    void activate(Volume volume);

    @Update("update volume set taint = #{taint} where id = #{id}")
    void taint(Volume volume);

    @Delete("delete from volume where id = #{id}")
    void deleteById(String id);

    @Delete("delete from volume where name = #{name}")
    void deleteByName(String name);
}
