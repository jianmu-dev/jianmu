package dev.jianmu.infrastructure.mapper.task;

import dev.jianmu.infrastructure.typehandler.ParameterSetTypeHandler;
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
    @Insert("insert into task_instance(id, serial_no, def_key, async_task_ref, business_id, trigger_id, start_time, end_time, status, output_parameters) " +
            "values(#{id}, #{serialNo}, #{defKey}, #{asyncTaskRef}, #{businessId}, #{triggerId}, #{startTime}, #{endTime}, #{status}, " +
            "#{outputParameters, jdbcType=BLOB,typeHandler=dev.jianmu.infrastructure.typehandler.ParameterSetTypeHandler})")
    void add(TaskInstance taskInstance);

    @Update("update task_instance set status = #{status}, end_time = #{endTime} where id = #{id}")
    void updateStatus(TaskInstance taskInstance);

    @Update("update task_instance set result_file = #{resultFile}, status = #{status}, end_time = #{endTime}, " +
            "output_parameters = #{outputParameters, jdbcType=BLOB,typeHandler=dev.jianmu.infrastructure.typehandler.ParameterSetTypeHandler} where id = #{id}")
    void saveSucceeded(TaskInstance taskInstance);

    @Select("select * from task_instance where async_task_ref = #{asyncTaskRef} and business_id = #{businessId} order by end_time desc limit 1")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "business_id", property = "businessId")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    @Result(column = "output_parameters", property = "outputParameters", typeHandler = ParameterSetTypeHandler.class)
    Optional<TaskInstance> limitByAsyncTaskRefAndBusinessId(@Param("asyncTaskRef") String asyncTaskRef, @Param("businessId") String businessId);

    @Select("select * from task_instance where id = #{instanceId}")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "business_id", property = "businessId")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    @Result(column = "output_parameters", property = "outputParameters", typeHandler = ParameterSetTypeHandler.class)
    Optional<TaskInstance> findById(String instanceId);

    @Select("select * from task_instance where business_id = #{businessId} order by start_time asc")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "business_id", property = "businessId")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    @Result(column = "output_parameters", property = "outputParameters", typeHandler = ParameterSetTypeHandler.class)
    List<TaskInstance> findByBusinessId(String businessId);

    @Select("select * from task_instance where async_task_ref = #{asyncTaskRef} and business_id = #{businessId}")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "business_id", property = "businessId")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    @Result(column = "output_parameters", property = "outputParameters", typeHandler = ParameterSetTypeHandler.class)
    List<TaskInstance> findByAsyncTaskRefAndBusinessId(@Param("asyncTaskRef") String asyncTaskRef, @Param("businessId") String businessId);


    @Select("select * from task_instance")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "business_id", property = "businessId")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    @Result(column = "output_parameters", property = "outputParameters", typeHandler = ParameterSetTypeHandler.class)
    List<TaskInstance> findAll(
            @Param("pageNum") int pageNum,
            @Param("pageSize") int pageSize
    );
}
