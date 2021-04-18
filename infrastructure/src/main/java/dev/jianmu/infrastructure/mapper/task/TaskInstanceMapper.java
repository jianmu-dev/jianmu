package dev.jianmu.infrastructure.mapper.task;

import dev.jianmu.task.aggregate.InstanceStatus;
import dev.jianmu.task.aggregate.TaskInstance;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * @class: InstanceMapper
 * @description: 任务实例Mapper
 * @author: Ethan Liu
 * @create: 2021-03-25 21:39
 **/
public interface TaskInstanceMapper {
    @Insert("insert into task_instance(id, def_key, business_id, trigger_id, start_time, end_time, status) " +
            "values(#{id}, #{defKey}, #{businessId}, #{triggerId}, #{startTime}, #{endTime}, #{status})")
    void add(TaskInstance taskInstance);

    @Update("update task_instance set status = #{status}, end_time = #{endTime} where id = #{id}")
    void updateStatus(TaskInstance taskInstance);

    @Select("select * from task_instance where id = #{instanceId}")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "business_id", property = "businessId")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    Optional<TaskInstance> findById(String instanceId);

    @Select("select * from task_instance where business_id = #{businessId}")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "business_id", property = "businessId")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    List<TaskInstance> findByBusinessId(String businessId);

    @Select("select * from task_instance where def_key = #{defKey}")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "business_id", property = "businessId")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    List<TaskInstance> findByDefKey(String defKey);

    @Select("select * from task_instance where def_key = #{defKey} and business_id = #{businessId}")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "business_id", property = "businessId")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    List<TaskInstance> findByDefKeyAndBusinessId(@Param("defKey") String defKey, @Param("businessId") String businessId);

    @Select("select * from task_instance where status = #{status}")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "business_id", property = "businessId")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    List<TaskInstance> findByStatus(InstanceStatus status);

    @Select("select * from task_instance")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "business_id", property = "businessId")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    List<TaskInstance> findAll(
            @Param("pageNum") int pageNum,
            @Param("pageSize") int pageSize
    );
}
