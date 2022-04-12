package dev.jianmu.infrastructure.mapper.workflow;

import dev.jianmu.workflow.aggregate.process.AsyncTaskInstance;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * @author Ethan Liu
 * @class AsyncTaskInstanceMapper
 * @description 异步任务实例Mapper
 * @create 2022-01-03 09:38
 */
public interface AsyncTaskInstanceMapper {
    @Select("select * from async_task_instance where id = #{id}")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "workflow_instance_id", property = "workflowInstanceId")
    @Result(column = "failure_mode", property = "failureMode")
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "async_task_type", property = "asyncTaskType")
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "next_target", property = "nextTarget")
    @Result(column = "activating_time", property = "activatingTime")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    @Result(column = "_version", property = "version")
    Optional<AsyncTaskInstance> findById(String id);

    @Select("select * from async_task_instance where workflow_instance_id = #{instanceId}")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "workflow_instance_id", property = "workflowInstanceId")
    @Result(column = "failure_mode", property = "failureMode")
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "async_task_type", property = "asyncTaskType")
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "next_target", property = "nextTarget")
    @Result(column = "activating_time", property = "activatingTime")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    @Result(column = "_version", property = "version")
    List<AsyncTaskInstance> findByInstanceId(String instanceId);

    @Select("select * from async_task_instance where trigger_id = #{triggerId}")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "workflow_instance_id", property = "workflowInstanceId")
    @Result(column = "failure_mode", property = "failureMode")
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "async_task_type", property = "asyncTaskType")
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "next_target", property = "nextTarget")
    @Result(column = "activating_time", property = "activatingTime")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    @Result(column = "_version", property = "version")
    List<AsyncTaskInstance> findByTriggerId(String triggerId);

    @Select("select * from async_task_instance where trigger_id = #{triggerId} and async_task_ref = #{taskRef}")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "workflow_instance_id", property = "workflowInstanceId")
    @Result(column = "failure_mode", property = "failureMode")
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "async_task_type", property = "asyncTaskType")
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "next_target", property = "nextTarget")
    @Result(column = "activating_time", property = "activatingTime")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    @Result(column = "_version", property = "version")
    Optional<AsyncTaskInstance> findByTriggerIdAndTaskRef(@Param("triggerId") String triggerId, @Param("taskRef") String taskRef);

    @Insert("insert into async_task_instance(id, trigger_id, workflow_ref, workflow_version, workflow_instance_id, name, description, status, failure_mode, async_task_ref, async_task_type, serial_no, next_target, activating_time, start_time, end_time) " +
            "values(#{id}, #{triggerId}, #{workflowRef}, #{workflowVersion}, #{workflowInstanceId}, #{name}, #{description}, #{status}, #{failureMode}, #{asyncTaskRef}, #{asyncTaskType}, #{serialNo}, #{nextTarget}, #{activatingTime}, #{startTime}, #{endTime})")
    void add(AsyncTaskInstance asyncTaskInstance);

    @Insert("<script>" +
            "insert into async_task_instance(id, trigger_id, workflow_ref, workflow_version, workflow_instance_id, name, description, status, failure_mode, async_task_ref, async_task_type, serial_no, next_target, activating_time, start_time, end_time) values" +
            "<foreach collection='asyncTaskInstances' item='i' index='key' separator=','>" +
            "(#{i.id}, #{i.triggerId}, #{i.workflowRef}, #{i.workflowVersion}, #{i.workflowInstanceId}, #{i.name}, #{i.description}, #{i.status}, #{i.failureMode}, #{i.asyncTaskRef}, #{i.asyncTaskType}, #{i.serialNo}, #{i.nextTarget}, #{i.activatingTime}, #{i.startTime}, #{i.endTime})" +
            "</foreach>" +
            " </script>")
    void addAll(@Param("asyncTaskInstances") List<AsyncTaskInstance> asyncTaskInstances);

    @Select("select _version from async_task_instance where id = #{id}")
    int getVersion(String id);

    @Update("update async_task_instance " +
            "set status=#{ati.status},serial_no=#{ati.serialNo}," +
            "next_target=#{ati.nextTarget},start_time=#{ati.startTime},end_time=#{ati.endTime},_version=#{ati.version} " +
            "where id = #{ati.id} and _version = #{version}")
    boolean activateById(@Param("ati") AsyncTaskInstance asyncTaskInstance, @Param("version") int version);

    @Update("update async_task_instance " +
            "set status=#{ati.status},serial_no=#{ati.serialNo}," +
            "next_target=#{ati.nextTarget},start_time=#{ati.startTime},end_time=#{ati.endTime},_version=#{ati.version} " +
            "where id = #{ati.id} and _version = #{version}")
    boolean succeedById(@Param("ati") AsyncTaskInstance asyncTaskInstance, @Param("version") int version);

    @Update("update async_task_instance set status=#{status}, serial_no=#{serialNo}, next_target=#{nextTarget}, start_time=#{startTime}, end_time=#{endTime}, _version=#{version} where id=#{id}")
    void updateById(AsyncTaskInstance asyncTaskInstance);

    @Delete("delete from async_task_instance where workflow_instance_id = #{workflowInstanceId}")
    void deleteByWorkflowInstanceId(String workflowInstanceId);

    @Delete("delete from async_task_instance where workflow_ref = #{workflowRef}")
    void deleteByWorkflowRef(String workflowRef);
}
