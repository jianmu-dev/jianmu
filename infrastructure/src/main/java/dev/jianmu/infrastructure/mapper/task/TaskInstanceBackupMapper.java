package dev.jianmu.infrastructure.mapper.task;

import dev.jianmu.task.aggregate.TaskInstance;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;

/**
 * @author Ethan Liu
 * @class InstanceMapper
 * @description 任务实例Mapper
 * @create 2021-03-25 21:39
 */
public interface TaskInstanceBackupMapper {
    @Insert("insert into jm_task_instance_backup(id, serial_no, def_key, node_info, async_task_ref, workflow_ref, workflow_version, business_id, trigger_id, start_time, end_time, status, worker_id, _version) " +
            "values(#{id}, #{serialNo}, #{defKey}, #{nodeInfo, jdbcType=BLOB,typeHandler=dev.jianmu.infrastructure.typehandler.NodeInfoTypeHandler}, #{asyncTaskRef}, #{workflowRef}, #{workflowVersion}, #{businessId}, #{triggerId}, #{startTime}, #{endTime}, #{status}, #{workerId}, #{version})")
    void add(TaskInstance taskInstance);

    @Update("update jm_task_instance_backup set status = #{status}, end_time = #{endTime} where id = #{id}")
    void updateStatus(TaskInstance taskInstance);

    @Update("update jm_task_instance_backup set worker_id = #{workerId}, end_time = #{endTime} where id = #{id}")
    void updateWorkerId(TaskInstance taskInstance);

    @Update("update jm_task_instance_backup set end_time = #{endTime}, _version = _version + 1 where id = #{id} and _version = #{version}")
    boolean acceptTask(TaskInstance taskInstance);

    @Update("update jm_task_instance_backup set status = #{status}, end_time = #{endTime} where id = #{id}")
    void saveSucceeded(TaskInstance taskInstance);
}
