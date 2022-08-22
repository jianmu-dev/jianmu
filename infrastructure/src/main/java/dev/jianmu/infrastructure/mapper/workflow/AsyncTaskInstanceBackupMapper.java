package dev.jianmu.infrastructure.mapper.workflow;

import dev.jianmu.workflow.aggregate.process.AsyncTaskInstance;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author Ethan Liu
 * @class AsyncTaskInstanceMapper
 * @description 异步任务实例Mapper
 * @create 2022-01-03 09:38
 */
public interface AsyncTaskInstanceBackupMapper {
    @Insert("insert into jm_async_task_instance_backup(id, trigger_id, workflow_ref, workflow_version, workflow_instance_id, name, description, status, failure_mode, async_task_ref, async_task_type, serial_no, next_target, activating_time, start_time, end_time) " +
            "values(#{id}, #{triggerId}, #{workflowRef}, #{workflowVersion}, #{workflowInstanceId}, #{name}, #{description}, #{status}, #{failureMode}, #{asyncTaskRef}, #{asyncTaskType}, #{serialNo}, #{nextTarget}, #{activatingTime}, #{startTime}, #{endTime})")
    void add(AsyncTaskInstance asyncTaskInstance);

    @Insert("<script>" +
            "insert into jm_async_task_instance_backup(id, trigger_id, workflow_ref, workflow_version, workflow_instance_id, name, description, status, failure_mode, async_task_ref, async_task_type, serial_no, next_target, activating_time, start_time, end_time) values" +
            "<foreach collection='asyncTaskInstances' item='i' index='key' separator=','>" +
            "(#{i.id}, #{i.triggerId}, #{i.workflowRef}, #{i.workflowVersion}, #{i.workflowInstanceId}, #{i.name}, #{i.description}, #{i.status}, #{i.failureMode}, #{i.asyncTaskRef}, #{i.asyncTaskType}, #{i.serialNo}, #{i.nextTarget}, #{i.activatingTime}, #{i.startTime}, #{i.endTime})" +
            "</foreach>" +
            " </script>")
    void addAll(@Param("asyncTaskInstances") List<AsyncTaskInstance> asyncTaskInstances);

    @Update("update jm_async_task_instance_backup " +
            "set status=#{ati.status},serial_no=#{ati.serialNo}," +
            "next_target=#{ati.nextTarget},start_time=#{ati.startTime},end_time=#{ati.endTime},_version=#{ati.version} " +
            "where id = #{ati.id} and _version = #{version}")
    boolean activateById(@Param("ati") AsyncTaskInstance asyncTaskInstance, @Param("version") int version);

    @Update("update jm_async_task_instance_backup " +
            "set status=#{ati.status},serial_no=#{ati.serialNo}," +
            "next_target=#{ati.nextTarget},start_time=#{ati.startTime},end_time=#{ati.endTime},_version=#{ati.version} " +
            "where id = #{ati.id} and _version = #{version}")
    boolean succeedById(@Param("ati") AsyncTaskInstance asyncTaskInstance, @Param("version") int version);

    @Update("update jm_async_task_instance_backup set status=#{status}, serial_no=#{serialNo}, next_target=#{nextTarget}, start_time=#{startTime}, end_time=#{endTime}, _version=#{version} where id=#{id}")
    void updateById(AsyncTaskInstance asyncTaskInstance);
}
