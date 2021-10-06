package dev.jianmu.infrastructure.mapper.task;

import dev.jianmu.infrastructure.typehandler.NodeInfoTypeHandler;
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
    @Insert("insert into task_instance(id, serial_no, def_key, node_info, async_task_ref, workflow_ref, workflow_version, business_id, trigger_id, start_time, end_time, status) " +
            "values(#{id}, #{serialNo}, #{defKey}, #{nodeInfo, jdbcType=BLOB,typeHandler=dev.jianmu.infrastructure.typehandler.NodeInfoTypeHandler}, #{asyncTaskRef}, #{workflowRef}, #{workflowVersion}, #{businessId}, #{triggerId}, #{startTime}, #{endTime}, #{status})")
    void add(TaskInstance taskInstance);

    @Update("update task_instance set status = #{status}, end_time = #{endTime} where id = #{id}")
    void updateStatus(TaskInstance taskInstance);

    @Update("update task_instance set result_file = #{resultFile}, status = #{status}, end_time = #{endTime} where id = #{id}")
    void saveSucceeded(TaskInstance taskInstance);

    @Delete("delete from task_instance where workflow_ref = #{workflowRef}")
    void deleteByWorkflowRef(String workflowRef);

    @Select("select * from task_instance where id = #{instanceId}")
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "node_info", property = "nodeInfo", typeHandler = NodeInfoTypeHandler.class)
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "business_id", property = "businessId")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    Optional<TaskInstance> findById(String instanceId);

    @Select("select * from task_instance where business_id = #{businessId} order by start_time asc")
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "node_info", property = "nodeInfo", typeHandler = NodeInfoTypeHandler.class)
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "business_id", property = "businessId")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    List<TaskInstance> findByBusinessId(String businessId);

    @Select("select * from task_instance where status = 'RUNNING'")
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "node_info", property = "nodeInfo", typeHandler = NodeInfoTypeHandler.class)
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "business_id", property = "businessId")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    List<TaskInstance> findRunningTask();

    @Select("select * from task_instance where async_task_ref = #{asyncTaskRef} and business_id = #{businessId}")
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "node_info", property = "nodeInfo", typeHandler = NodeInfoTypeHandler.class)
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "business_id", property = "businessId")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    List<TaskInstance> findByAsyncTaskRefAndBusinessId(@Param("asyncTaskRef") String asyncTaskRef, @Param("businessId") String businessId);


    @Select("select * from task_instance")
    @Result(column = "serial_no", property = "serialNo")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "node_info", property = "nodeInfo", typeHandler = NodeInfoTypeHandler.class)
    @Result(column = "async_task_ref", property = "asyncTaskRef")
    @Result(column = "workflow_ref", property = "workflowRef")
    @Result(column = "workflow_version", property = "workflowVersion")
    @Result(column = "business_id", property = "businessId")
    @Result(column = "trigger_id", property = "triggerId")
    @Result(column = "start_time", property = "startTime")
    @Result(column = "end_time", property = "endTime")
    List<TaskInstance> findAll(
            @Param("pageNum") int pageNum,
            @Param("pageSize") int pageSize
    );
}
