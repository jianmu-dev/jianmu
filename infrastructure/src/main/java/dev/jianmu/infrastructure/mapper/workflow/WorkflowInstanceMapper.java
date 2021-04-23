package dev.jianmu.infrastructure.mapper.workflow;

import dev.jianmu.infrastructure.typehandler.TaskInstanceListTypeHandler;
import dev.jianmu.workflow.aggregate.process.ProcessStatus;
import dev.jianmu.workflow.aggregate.process.WorkflowInstance;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * @class: WorkflowInstanceMapper
 * @description: 流程实例DB映射接口
 * @author: Ethan Liu
 * @create: 2021-03-21 19:27
 **/
public interface WorkflowInstanceMapper {

    @Select("select * from workflow_instance where workflow_ref = #{workflowRef} " +
            "and workflow_version = #{workflowVersion} and status = #{status}")
    @Result(column = "task_instances", property = "asyncTaskInstances", typeHandler = TaskInstanceListTypeHandler.class)
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "run_mode", property = "runMode")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    List<WorkflowInstance> findByRefAndVersionAndStatus(
            @Param("workflowRef") String workflowRef,
            @Param("workflowVersion") String workflowVersion,
            @Param("status") ProcessStatus status
    );

    @Select("select * from workflow_instance where id = #{instanceId}")
    @Result(column = "task_instances", property = "asyncTaskInstances", typeHandler = TaskInstanceListTypeHandler.class)
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "run_mode", property = "runMode")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    Optional<WorkflowInstance> findById(String instanceId);

    @Insert("insert into workflow_instance(id, trigger_id, name, description, run_mode, status, workflow_ref, workflow_version, task_instances, start_time, end_time, _version) " +
            "values(#{wk.id},#{wk.triggerId},#{wk.name},#{wk.description},#{wk.runMode},#{wk.status},#{wk.workflowRef},#{wk.workflowVersion}," +
            "#{wk.asyncTaskInstances, jdbcType=BLOB,typeHandler=dev.jianmu.infrastructure.typehandler.TaskInstanceListTypeHandler}," +
            "#{wk.startTime},#{wk.endTime},#{version})")
    boolean add(@Param("wk") WorkflowInstance workflowInstance, @Param("version") int version);

    @Select("select _version from workflow_instance where id = #{id}")
    int getVersion(String id);

    @Update("update workflow_instance " +
            "set run_mode=#{wk.runMode},status=#{wk.status}," +
            "task_instances=#{wk.asyncTaskInstances , jdbcType=BLOB,typeHandler=dev.jianmu.infrastructure.typehandler.TaskInstanceListTypeHandler}," +
            "end_time=#{wk.endTime},_version= _version+1 " +
            "where id = #{wk.id} and _version = #{version}")
    boolean save(@Param("wk") WorkflowInstance workflowInstance,@Param("version") int version);

    @Select("select * from workflow_instance")
    @Result(column = "task_instances", property = "asyncTaskInstances", typeHandler = TaskInstanceListTypeHandler.class)
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "run_mode", property = "runMode")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    List<WorkflowInstance> findAll(
            @Param("pageNum") int pageNum,
            @Param("pageSize") int pageSize
    );

    @Select("select * from workflow_instance where status = #{status}")
    @Result(column = "task_instances", property = "asyncTaskInstances", typeHandler = TaskInstanceListTypeHandler.class)
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "run_mode", property = "runMode")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    List<WorkflowInstance> findAllPage(ProcessStatus status);
}
