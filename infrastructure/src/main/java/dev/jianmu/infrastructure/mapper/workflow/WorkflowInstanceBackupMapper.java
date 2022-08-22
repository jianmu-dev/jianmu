package dev.jianmu.infrastructure.mapper.workflow;

import dev.jianmu.workflow.aggregate.process.WorkflowInstance;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author Ethan Liu
 * @class WorkflowInstanceMapper
 * @description 流程实例DB映射接口
 * @create 2021-03-21 19:27
 */
public interface WorkflowInstanceBackupMapper {
    @Insert("insert into jm_workflow_instance_backup(id, serial_no, trigger_id, trigger_type, name, description, run_mode, status, workflow_ref, workflow_version, start_time, suspended_time, end_time, _version) " +
            "values(#{wk.id},#{wk.serialNo},#{wk.triggerId},#{wk.triggerType},#{wk.name},#{wk.description},#{wk.runMode},#{wk.status},#{wk.workflowRef},#{wk.workflowVersion}," +
            "#{wk.startTime},#{wk.suspendedTime},#{wk.endTime},#{version})")
    boolean add(@Param("wk") WorkflowInstance workflowInstance, @Param("version") int version);

    @Update("update jm_workflow_instance_backup " +
            "set run_mode=#{wk.runMode},status=#{wk.status},start_time=#{wk.startTime}," +
            "global_parameters = #{wk.globalParameters, jdbcType=BLOB,typeHandler=dev.jianmu.infrastructure.typehandler.ParameterSetTypeHandler}," +
            "suspended_time=#{wk.suspendedTime},end_time=#{wk.endTime},_version= _version+1 " +
            "where id = #{wk.id}")
    void save(@Param("wk") WorkflowInstance workflowInstance);
}
