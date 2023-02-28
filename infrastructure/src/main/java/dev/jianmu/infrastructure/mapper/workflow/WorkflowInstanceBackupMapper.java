package dev.jianmu.infrastructure.mapper.workflow;

import dev.jianmu.workflow.aggregate.process.WorkflowInstance;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Ethan Liu
 * @class WorkflowInstanceMapper
 * @description 流程实例DB映射接口
 * @create 2021-03-21 19:27
 */
public interface WorkflowInstanceBackupMapper {
    @Insert("insert into jm_workflow_instance_backup(id, serial_no, trigger_id, trigger_type, name, description, run_mode, status, workflow_ref, workflow_version,occurred_time, start_time, suspended_time, end_time, _version) " +
            "values(#{wk.id},#{wk.serialNo},#{wk.triggerId},#{wk.triggerType},#{wk.name},#{wk.description},#{wk.runMode},#{wk.status},#{wk.workflowRef},#{wk.workflowVersion}," +
            "#{wk.occurredTime},#{wk.startTime},#{wk.suspendedTime},#{wk.endTime},#{version})")
    boolean add(@Param("wk") WorkflowInstance workflowInstance, @Param("version") int version);

    @Update("update jm_workflow_instance_backup " +
            "set run_mode=#{wk.runMode},status=#{wk.status},start_time=#{wk.startTime}," +
            "global_parameters = #{wk.globalParameters, jdbcType=BLOB,typeHandler=dev.jianmu.infrastructure.typehandler.ParameterSetTypeHandler}," +
            "suspended_time=#{wk.suspendedTime},end_time=#{wk.endTime},_version= _version+1 " +
            "where id = #{wk.id}")
    void save(@Param("wk") WorkflowInstance workflowInstance);

    @Select("SELECT * FROM jm_workflow_instance_backup where workflow_ref=#{workflowRef} and serial_no < ((select max(serial_no) from jm_workflow_instance_backup where workflow_ref=#{workflowRef}) - #{offset})")
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "trigger_type", property = "triggerType")
    @Result(column = "run_mode", property = "runMode")
    @Result(column = "occurred_time", property = "occurredTime")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "suspended_time", property = "suspendedTime")
    @Result(column = "end_time", property = "endTime")
    List<WorkflowInstance> findByRefOffset(@Param("workflowRef") String workflowRef, @Param("offset") long offset);
}
