package dev.jianmu.infrastructure.mapper.task;

import dev.jianmu.task.aggregate.Volume;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * @author <a href="https://gitee.com/ethan-liu">Ethan Liu</a>
 * @date 2023-02-19 11:10
 */
public interface VolumeMapper {

    @Insert("insert into volume(id, name, scope, workflow_ref, worker_id, available, taint, cleaning, created_time, available_time) " +
            "values(#{id}, #{name}, #{scope}, #{workflowRef}, #{workerId}, #{available}, #{taint}, #{cleaning}, #{createdTime}, #{availableTime})")
    void create(Volume volume);

    @Select("select * from volume where id = #{id}")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "worker_id", property = "workerId")
    @Result(column = "created_time", property = "createdTime")
    @Result(column = "available_time", property = "availableTime")
    Optional<Volume> findById(String id);

    @Select("select * from volume where name = #{name} and workflow_ref = #{workflowRef}")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "worker_id", property = "workerId")
    @Result(column = "created_time", property = "createdTime")
    @Result(column = "available_time", property = "availableTime")
    Optional<Volume> findByNameAndWorkflowRef(@Param("name") String name, @Param("workflowRef") String workflowRef);

    @Select("select * from volume where workflow_ref = #{workflowRef}")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "worker_id", property = "workerId")
    @Result(column = "created_time", property = "createdTime")
    @Result(column = "available_time", property = "availableTime")
    List<Volume> findByWorkflowRef(String workflowRef);

    @Select("select * from volume where workflow_ref = #{workflowRef} and scope = #{scope}")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "worker_id", property = "workerId")
    @Result(column = "created_time", property = "createdTime")
    @Result(column = "available_time", property = "availableTime")
    List<Volume> findByWorkflowRefAndScope(@Param("workflowRef") String workflowRef, @Param("scope") Volume.Scope scope);

    @Update("update volume set available = #{available}, cleaning = #{cleaning}, worker_id = #{workerId}, available_time = #{availableTime} where id = #{id}")
    void activate(Volume volume);

    @Update("update volume set taint = #{taint} where id = #{id}")
    void taint(Volume volume);

    @Update("update volume set cleaning = #{cleaning}, available = #{available} where id = #{id}")
    void clean(Volume volume);

    @Delete("delete from volume where id = #{id}")
    void deleteById(String id);

    @Delete("delete from volume where name = #{name} and workflow_ref = #{workflowRef}")
    void deleteByNameAndWorkflowRef(@Param("name") String name, @Param("workflowRef") String workflowRef);
}
