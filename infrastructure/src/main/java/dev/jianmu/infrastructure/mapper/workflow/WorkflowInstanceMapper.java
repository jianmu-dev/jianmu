package dev.jianmu.infrastructure.mapper.workflow;

import dev.jianmu.infrastructure.typehandler.TaskInstanceListTypeHandler;
import dev.jianmu.workflow.aggregate.process.ProcessStatus;
import dev.jianmu.workflow.aggregate.process.WorkflowInstance;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * @class WorkflowInstanceMapper
 * @description 流程实例DB映射接口
 * @author Ethan Liu
 * @create 2021-03-21 19:27
*/
public interface WorkflowInstanceMapper {

    @Select("select * from workflow_instance where workflow_ref = #{workflowRef} " +
            "and workflow_version = #{workflowVersion} and status = #{status}")
    @Result(column = "task_instances", property = "asyncTaskInstances", typeHandler = TaskInstanceListTypeHandler.class)
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "trigger_type", property = "triggerType")
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
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "trigger_type", property = "triggerType")
    @Result(column = "run_mode", property = "runMode")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    Optional<WorkflowInstance> findById(String instanceId);

    @Select("select * from workflow_instance where trigger_id = #{triggerId}")
    @Result(column = "task_instances", property = "asyncTaskInstances", typeHandler = TaskInstanceListTypeHandler.class)
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "trigger_type", property = "triggerType")
    @Result(column = "run_mode", property = "runMode")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    Optional<WorkflowInstance> findByTriggerId(String triggerId);

    @Insert("insert into workflow_instance(id, serial_no, trigger_id, trigger_type, name, description, run_mode, status, workflow_ref, workflow_version, task_instances, start_time, end_time, _version) " +
            "values(#{wk.id},#{wk.serialNo},#{wk.triggerId},#{wk.triggerType},#{wk.name},#{wk.description},#{wk.runMode},#{wk.status},#{wk.workflowRef},#{wk.workflowVersion}," +
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
    boolean save(@Param("wk") WorkflowInstance workflowInstance, @Param("version") int version);

    @Delete("delete from workflow_instance where workflow_ref=#{workflowRef}")
    void deleteByWorkflowRef(String workflowRef);

    @Select("select * from workflow_instance")
    @Result(column = "task_instances", property = "asyncTaskInstances", typeHandler = TaskInstanceListTypeHandler.class)
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "trigger_type", property = "triggerType")
    @Result(column = "run_mode", property = "runMode")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    List<WorkflowInstance> findAll(
            @Param("pageNum") int pageNum,
            @Param("pageSize") int pageSize
    );

    @Select("<script>" +
            "SELECT * FROM `workflow_instance` " +
            "<where>" +
            "<if test='status != null'>status = #{status}</if>" +
            "<if test='!id.isBlank()'> AND `id` like concat('%', #{id}, '%')</if>" +
            "<if test='!name.isBlank()'> AND `name` like concat('%', #{name}, '%')</if>" +
            "<if test='!workflowVersion.isBlank()'> AND `workflow_version` like concat('%', #{workflowVersion}, '%')</if>" +
            "</where>" +
            " order by end_time desc" +
            "</script>")
    @Result(column = "task_instances", property = "asyncTaskInstances", typeHandler = TaskInstanceListTypeHandler.class)
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "trigger_type", property = "triggerType")
    @Result(column = "run_mode", property = "runMode")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    List<WorkflowInstance> findAllPage(
            @Param("id") String id,
            @Param("name") String name,
            @Param("workflowVersion") String workflowVersion,
            @Param("status") ProcessStatus status
    );

    @Select("select * from workflow_instance where workflow_ref = #{workflowRef} order by serial_no desc")
    @Result(column = "task_instances", property = "asyncTaskInstances", typeHandler = TaskInstanceListTypeHandler.class)
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "trigger_type", property = "triggerType")
    @Result(column = "run_mode", property = "runMode")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    List<WorkflowInstance> findByWorkflowRef(@Param("workflowRef") String workflowRef);

    @Select("select * from workflow_instance where workflow_ref = #{workflowRef} order by serial_no desc limit 1")
    @Result(column = "task_instances", property = "asyncTaskInstances", typeHandler = TaskInstanceListTypeHandler.class)
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "trigger_type", property = "triggerType")
    @Result(column = "run_mode", property = "runMode")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    Optional<WorkflowInstance> findByRefAndSerialNoMax(@Param("workflowRef") String workflowRef);
}
