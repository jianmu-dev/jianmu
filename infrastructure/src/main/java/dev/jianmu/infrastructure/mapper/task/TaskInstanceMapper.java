package dev.jianmu.infrastructure.mapper.task;

import dev.jianmu.infrastructure.typehandler.NodeInfoTypeHandler;
import dev.jianmu.task.aggregate.InstanceStatus;
import dev.jianmu.task.aggregate.TaskInstance;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * @author Ethan Liu
 * @class InstanceMapper
 * @description 任务实例Mapper
 * @create 2021-03-25 21:39
 */
public interface TaskInstanceMapper {
    @Insert("insert into task_instance(id, serial_no, def_key, node_info, async_task_ref, workflow_ref, workflow_version, business_id, trigger_id, start_time, end_time, status, worker_id, _version) " +
            "values(#{id}, #{serialNo}, #{defKey}, #{nodeInfo, jdbcType=BLOB,typeHandler=dev.jianmu.infrastructure.typehandler.NodeInfoTypeHandler}, #{asyncTaskRef}, #{workflowRef}, #{workflowVersion}, #{businessId}, #{triggerId}, #{startTime}, #{endTime}, #{status}, #{workerId}, #{version})")
    void add(TaskInstance taskInstance);

    @Update("update task_instance set status = #{status}, end_time = #{endTime} where id = #{id}")
    void updateStatus(TaskInstance taskInstance);

    @Update("update task_instance set worker_id = #{workerId}, status = 'WAITING', end_time = #{endTime} where id = #{id} and status = 'INIT'")
    boolean updateWorkerId(TaskInstance taskInstance);

    @Update("update task_instance set end_time = #{endTime}, _version = _version + 1 where id = #{id} and _version = #{version}")
    boolean acceptTask(TaskInstance taskInstance);

    @Update("update task_instance set status = #{status}, end_time = #{endTime} where id = #{id}")
    void saveSucceeded(TaskInstance taskInstance);

    @Delete("delete from task_instance where workflow_ref = #{workflowRef}")
    void deleteByWorkflowRef(String workflowRef);

    @Delete("delete from task_instance where trigger_id = #{triggerId}")
    void deleteByTriggerId(String triggerId);

    @Select("select * from task_instance where id = #{instanceId}")
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "node_info", property = "nodeInfo", typeHandler = NodeInfoTypeHandler.class)
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "business_id", property = "businessId")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "worker_id", property = "workerId")
    @Result(column = "_version", property = "version")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    Optional<TaskInstance> findById(String instanceId);

    @Select("select * from task_instance where business_id = #{businessId}")
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "node_info", property = "nodeInfo", typeHandler = NodeInfoTypeHandler.class)
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "business_id", property = "businessId")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "worker_id", property = "workerId")
    @Result(column = "_version", property = "version")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    List<TaskInstance> findByBusinessId(String businessId);

    @Select("select * from task_instance where business_id = #{businessId} order by serial_no desc limit 1")
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "node_info", property = "nodeInfo", typeHandler = NodeInfoTypeHandler.class)
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "business_id", property = "businessId")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "worker_id", property = "workerId")
    @Result(column = "_version", property = "version")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    Optional<TaskInstance> findByBusinessIdAndMaxSerialNo(String businessId);

    @Select("select * from task_instance where trigger_id = #{triggerId} order by start_time asc")
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "node_info", property = "nodeInfo", typeHandler = NodeInfoTypeHandler.class)
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "business_id", property = "businessId")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "worker_id", property = "workerId")
    @Result(column = "_version", property = "version")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    List<TaskInstance> findByTriggerId(String triggerId);

    @Select("select * from task_instance where status = 'RUNNING'")
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "node_info", property = "nodeInfo", typeHandler = NodeInfoTypeHandler.class)
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "business_id", property = "businessId")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "worker_id", property = "workerId")
    @Result(column = "_version", property = "version")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    List<TaskInstance> findRunningTask();

    @Select("select * from task_instance")
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "node_info", property = "nodeInfo", typeHandler = NodeInfoTypeHandler.class)
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "business_id", property = "businessId")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "worker_id", property = "workerId")
    @Result(column = "_version", property = "version")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    List<TaskInstance> findAll(
            @Param("pageNum") int pageNum,
            @Param("pageSize") int pageSize
    );

    @Select("<script>" +
            "select * from task_instance " +
            "<where> " +
            "worker_id = #{workerId} and status = 'WAITING' And _version = 0 " +
            "<if test='triggerId != null'> AND `trigger_id` = #{triggerId} </if>" +
            "</where>" +
            "limit 1" +
            "</script>")
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "node_info", property = "nodeInfo", typeHandler = NodeInfoTypeHandler.class)
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "business_id", property = "businessId")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "worker_id", property = "workerId")
    @Result(column = "_version", property = "version")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    Optional<TaskInstance> findByWorkerIdAndTriggerIdLimit(@Param("workerId") String workerId, @Param("triggerId") String triggerId);

    @Select("select * from task_instance where business_id = #{businessId} and status = 'WAITING' and _version = #{version}")
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "node_info", property = "nodeInfo", typeHandler = NodeInfoTypeHandler.class)
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "business_id", property = "businessId")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "worker_id", property = "workerId")
    @Result(column = "_version", property = "version")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    Optional<TaskInstance> findByBusinessIdAndVersion(@Param("businessId") String businessId, @Param("version") int version);

    @Select("select * from task_instance where trigger_id = #{triggerId} and status = #{status}")
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "node_info", property = "nodeInfo", typeHandler = NodeInfoTypeHandler.class)
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "business_id", property = "businessId")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "worker_id", property = "workerId")
    @Result(column = "_version", property = "version")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    List<TaskInstance> findByTriggerIdAndStatus(@Param("triggerId") String triggerId, @Param("status") InstanceStatus status);

    @Select("select * from task_instance where workflow_ref = #{workflowRef}")
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "node_info", property = "nodeInfo", typeHandler = NodeInfoTypeHandler.class)
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "business_id", property = "businessId")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "worker_id", property = "workerId")
    @Result(column = "_version", property = "version")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    List<TaskInstance> findByWorkflowRef(String workflowRef);
}
