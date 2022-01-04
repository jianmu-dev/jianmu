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
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "async_task_type", property = "asyncTaskType")
    @Result(column = "activating_time", property = "activatingTime")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    Optional<AsyncTaskInstance> findById(String id);

    @Select("select * from async_task_instance where workflow_instance_id = #{instanceId}")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "workflow_instance_id", property = "workflowInstanceId")
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "async_task_type", property = "asyncTaskType")
    @Result(column = "activating_time", property = "activatingTime")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    List<AsyncTaskInstance> findByInstanceId(String instanceId);

    @Select("select * from async_task_instance where trigger_id = #{triggerId}")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "workflow_instance_id", property = "workflowInstanceId")
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "async_task_type", property = "asyncTaskType")
    @Result(column = "activating_time", property = "activatingTime")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    List<AsyncTaskInstance> findByTriggerId(String triggerId);

    @Insert("insert into async_task_instance(id, trigger_id, workflow_ref, workflow_version, workflow_instance_id, name, description, status, async_task_ref, async_task_type, activating_time, start_time, end_time) " +
            "values(#{id}, #{triggerId}, #{workflowRef}, #{workflowVersion}, #{workflowInstanceId}, #{name}, #{description}, #{status}, #{asyncTaskRef}, #{asyncTaskType}, #{activatingTime}, #{startTime}, #{endTime})")
    void add(AsyncTaskInstance asyncTaskInstance);

    @Update("update async_task_instance set status=#{status}, start_time=#{startTime}, end_time=#{endTime} where id=#{id}")
    void updateById(AsyncTaskInstance asyncTaskInstance);

    @Delete("delete from async_task_instance where workflow_instance_id = #{workflowInstanceId}")
    void deleteByWorkflowInstanceId(String workflowInstanceId);

    @Delete("delete from async_task_instance where workflow_ref = #{workflowRef}")
    void deleteByWorkflowRef(String workflowRef);
}
