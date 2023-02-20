package dev.jianmu.infrastructure.mapper.task;

import dev.jianmu.task.aggregate.Volume;
import org.apache.ibatis.annotations.*;

import java.util.Optional;

/**
 * @author <a href="https://gitee.com/ethan-liu">Ethan Liu</a>
 * @date 2023-02-19 11:10
 */
public interface VolumeMapper {

    @Insert("insert into jm_volume(id, name, scope, worker_id, available, taint, created_time, available_time) " +
            "values(#{id}, #{name}, #{scope}, #{workerId}, #{available}, #{taint}, #{createdTime}, #{availableTime})")
    void create(Volume volume);

    @Select("select * from jm_volume where id = #{id}")
    @Result(column = "worker_id", property = "workerId")
    @Result(column = "created_time", property = "createdTime")
    @Result(column = "available_time", property = "availableTime")
    Optional<Volume> findById(String id);

    @Select("select * from jm_volume where name = #{name}")
    @Result(column = "worker_id", property = "workerId")
    @Result(column = "created_time", property = "createdTime")
    @Result(column = "available_time", property = "availableTime")
    Optional<Volume> findByName(String name);

    @Update("update jm_volume set available = #{available} where name = #{name}")
    void activate(Volume volume);

    @Update("update jm_volume set taint = #{taint} where name = #{name}")
    void taint(Volume volume);

    @Delete("delete from jm_volume where id = #{id}")
    void deleteById(String id);

    @Delete("delete from jm_volume where name = #{name}")
    void deleteByName(String name);
}
